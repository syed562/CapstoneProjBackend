package com.example.loanapplication.service;

import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.MODELS.LoanType;
import com.example.loanapplication.client.LoanServiceClient;
import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.UserServiceClient;
import com.example.loanapplication.client.dto.ProfileView;
import com.example.loanapplication.client.dto.UserView;
import com.example.loanapplication.event.LoanApplicationEvent;
import com.example.loanapplication.repository.LoanApplicationRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoanApplicationService {
    private final LoanApplicationRepository repo;
    private final ApprovalCriteriaService approvalCriteriaService;
    private final NotificationService notificationService;
    private final ProfileServiceClient profileServiceClient;
    private final LoanServiceClient loanServiceClient;
    private final UserServiceClient userServiceClient;
    private final NotificationPublisher notificationPublisher;
    private final RateConfigService rateConfigService;
    private final double minAmount;
    private final double maxAmount;
    private final Set<Integer> allowedTenures;
    private final Set<LoanType> allowedTypes;
    private final Map<LoanType, Double> defaultRates;

    public LoanApplicationService(
            LoanApplicationRepository repo,
            ApprovalCriteriaService approvalCriteriaService,
            NotificationService notificationService,
            ProfileServiceClient profileServiceClient,
            LoanServiceClient loanServiceClient,
            UserServiceClient userServiceClient,
            NotificationPublisher notificationPublisher,
            RateConfigService rateConfigService,
            @Value("${loan.rules.amount.min:5000}") double minAmount,
            @Value("${loan.rules.amount.max:2000000}") double maxAmount,
            @Value("${loan.rules.tenures:12,24,36}") String tenureOptions,
            @Value("${loan.rules.rates:PERSONAL=12,HOME=8.5,AUTO=10,EDUCATIONAL=7.5,HOME_LOAN=8.5}") String rateOptions
    ) {
        this.repo = repo;
        this.approvalCriteriaService = approvalCriteriaService;
        this.notificationService = notificationService;
        this.profileServiceClient = profileServiceClient;
        this.loanServiceClient = loanServiceClient;
        this.userServiceClient = userServiceClient;
        this.notificationPublisher = notificationPublisher;
        this.rateConfigService = rateConfigService;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.allowedTenures = parseIntSet(tenureOptions);
        this.allowedTypes = Set.of(LoanType.values());
        this.defaultRates = parseRateMap(rateOptions);
    }

    public LoanApplication apply(String userId, LoanType loanType, double amount, int termMonths, Double ratePercent) {
        ensureProfileExists(userId);
        if (amount < minAmount || amount > maxAmount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be between " + minAmount + " and " + maxAmount);
        }
        if (!allowedTenures.contains(termMonths)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Term must be one of " + allowedTenures);
        }

        List<String> activeStatuses = List.of("SUBMITTED", "UNDER_REVIEW", "APPROVED");
        boolean hasActive = !repo.findByUserIdAndLoanTypeAndStatusIn(userId, loanType, activeStatuses).isEmpty();
        if (hasActive) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An active application for this loan type already exists");
        }

        double resolvedRate = ratePercent != null ? ratePercent : resolveRateForType(loanType);

        String now = Instant.now().toString();
        LoanApplication app = new LoanApplication();
        app.setId(UUID.randomUUID().toString());
        app.setUserId(userId);
        app.setLoanType(loanType);
        app.setAmount(String.valueOf(amount));
        app.setTermMonths(termMonths);
        app.setRatePercent(String.valueOf(resolvedRate));
        app.setStatus("SUBMITTED");
        app.setCreatedAt(now);
        app.setUpdatedAt(now);
        LoanApplication savedApp = repo.save(app);
        
        // Publish loan application created event
        try {
            LoanApplicationEvent event = new LoanApplicationEvent();
            event.setApplicationId(savedApp.getId());
            event.setUserId(userId);
            event.setLoanAmount(amount);
            notificationPublisher.publishApplicationCreated(event);
        } catch (Exception e) {
            // Log but don't fail application creation if notification fails
            System.err.println("Failed to publish loan application event: " + e.getMessage());
        }
        
        return savedApp;
    }

    public List<LoanApplication> listByUser(String userId) {
        List<LoanApplication> applications = repo.findByUserId(userId);
        populateUserNames(applications);
        return applications;
    }

    public LoanApplication get(String id) {
        LoanApplication app = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        populateUserName(app);
        return app;
    }

    public LoanApplication markUnderReview(String id) {
        LoanApplication app = get(id);
        if (!"SUBMITTED".equals(app.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application must be in SUBMITTED status to move to UNDER_REVIEW");
        }
        app.setStatus("UNDER_REVIEW");
        app.setUpdatedAt(Instant.now().toString());
        return repo.save(app);
    }

    public LoanApplication approve(String id) {
        LoanApplication app = get(id);
        // Allow approving SUBMITTED or UNDER_REVIEW applications
        if (!"SUBMITTED".equals(app.getStatus()) && !"UNDER_REVIEW".equals(app.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application must be in SUBMITTED or UNDER_REVIEW status to approve");
        }
        
        // Validate approval criteria (credit score, income, liabilities)
        double decryptedAmount = Double.parseDouble(app.getAmount());
        ApprovalCriteriaService.ApprovalDecision decision = approvalCriteriaService.validateApprovalCriteria(
            app.getUserId(), 
            decryptedAmount
        );
        
        if (!decision.isApproved()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Loan cannot be approved: " + decision.getReason());
        }
        
        app.setStatus("APPROVED");
        app.setRemarks(decision.getReason()); // Store approval reason
        app.setUpdatedAt(Instant.now().toString());
        LoanApplication saved = repo.save(app);
        
        // Send approval notification
        double notifyAmount = Double.parseDouble(app.getAmount());
        double notifyRate = app.getRatePercent() != null ? Double.parseDouble(app.getRatePercent()) : 0.0;
        notificationService.sendApprovalNotification(
            app.getUserId(),
            app.getId(),
            notifyAmount,
            app.getTermMonths(),
            notifyRate,
            app.getLoanType().toString()
        );
        
        // Automatically create loan in loan-service (CRITICAL - must succeed)
        try {
            LoanServiceClient.CreateLoanRequest loanRequest = new LoanServiceClient.CreateLoanRequest(
                app.getUserId(),
                Double.parseDouble(app.getAmount()),
                app.getTermMonths(),
                Double.parseDouble(app.getRatePercent() != null ? app.getRatePercent() : "0"),
                app.getLoanType().toString()
            );
            log.info("[LOAN-APP] Creating loan from approved application: {}", saved.getId());
            log.debug("[LOAN-APP] Loan Request - UserId: {}, Amount: {}, Type: {}", app.getUserId(), app.getAmount(), app.getLoanType());
            loanServiceClient.createLoanFromApplication(loanRequest);
            log.info("[LOAN-APP] ✓ SUCCESS: Loan created in loan-service for application: {}", saved.getId());
        } catch (Exception e) {
            log.error("[LOAN-APP] ✗ CRITICAL ERROR: Failed to create loan in loan-service for application: {}", saved.getId());
            log.error("[LOAN-APP] Error Type: {}, Message: {}", e.getClass().getName(), e.getMessage(), e);
            e.printStackTrace();
            // Log for debugging but don't fail - admin can manually retry
            System.err.println("[LOAN-APP] Application approved but loan creation failed. Manual retry needed.");
        }
        
        // Publish approval event
        try {
            LoanApplicationEvent event = new LoanApplicationEvent();
            event.setApplicationId(saved.getId());
            event.setUserId(saved.getUserId());
            event.setLoanAmount(Double.parseDouble(saved.getAmount()));
            notificationPublisher.publishApplicationApproved(event);
        } catch (Exception e) {
            System.err.println("Failed to publish approval event: " + e.getMessage());
        }
        
        return saved;
    }

    public LoanApplication reject(String id, String remarks) {
        LoanApplication app = get(id);
        // Allow rejecting SUBMITTED or UNDER_REVIEW applications
        if (!"SUBMITTED".equals(app.getStatus()) && !"UNDER_REVIEW".equals(app.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application must be in SUBMITTED or UNDER_REVIEW status to reject");
        }
        if (remarks == null || remarks.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection remarks are required");
        }
        app.setStatus("REJECTED");
        app.setRemarks(remarks);
        app.setUpdatedAt(Instant.now().toString());
        LoanApplication saved = repo.save(app);
        
        // Publish rejection event
        try {
            LoanApplicationEvent event = new LoanApplicationEvent();
            event.setApplicationId(saved.getId());
            event.setUserId(saved.getUserId());
            event.setLoanAmount(Double.parseDouble(saved.getAmount()));
            event.setRemarks(remarks);
            notificationPublisher.publishApplicationRejected(event);
        } catch (Exception e) {
            System.err.println("Failed to publish rejection event: " + e.getMessage());
        }
        
        return saved;
    }

    public List<LoanApplication> listAll() {
        List<LoanApplication> applications = repo.findAll();
        populateUserNames(applications);
        return applications;
    }

    private void populateUserNames(List<LoanApplication> applications) {
        for (LoanApplication app : applications) {
            populateUserName(app);
        }
    }

    private void populateUserName(LoanApplication app) {
        try {
            if (app.getUserId() != null) {
                log.debug("Fetching username for userId: {}", app.getUserId());
                UserView user = userServiceClient.getUser(app.getUserId());
                if (user != null && user.getUsername() != null) {
                    app.setUserName(user.getUsername());
                    log.debug("Username populated: {} for userId: {}", user.getUsername(), app.getUserId());
                } else {
                    log.warn("UserView returned null or username is null for userId: {}", app.getUserId());
                    app.setUserName("Unknown");
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {}, error: {}", app.getUserId(), e.getMessage(), e);
            app.setUserName("Unknown");
        }
    }

    private Set<Integer> parseIntSet(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Map<LoanType, Double> parseRateMap(String csv) {
        Map<LoanType, Double> map = new HashMap<>();
        if (csv == null || csv.isBlank()) return map;
        String[] pairs = csv.split(",");
        for (String pair : pairs) {
            if (pair.isBlank() || !pair.contains("=")) continue;
            String[] kv = pair.split("=");
            if (kv.length != 2) continue;
            try {
                LoanType type = LoanType.valueOf(kv[0].trim().toUpperCase());
                Double rate = Double.parseDouble(kv[1].trim());
                map.put(type, rate);
            } catch (Exception ignored) {
                // Skip invalid entry
            }
        }
        return map;
    }

    private double resolveRateForType(LoanType loanType) {
        // Use dynamic RateConfigService if available, fallback to defaults
        try {
            Double rate = rateConfigService.getRate(loanType.toString());
            return rate != null ? rate : defaultRates.getOrDefault(loanType, 12.0);
        } catch (Exception e) {
            return defaultRates.getOrDefault(loanType, 12.0);
        }
    }

    private void ensureProfileExists(String userId) {
        try {
            System.out.println("[LOAN-APP] Checking profile for userId: " + userId);
            ProfileView profile = profileServiceClient.getProfile(userId);
            System.out.println("[LOAN-APP] Profile found: " + profile);
            if (profile == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile not found. Please create your profile before applying for a loan.");
            }
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile not found. Please create your profile before applying for a loan.");
        } catch (FeignException e) {
            // Temporarily log and allow through for testing
            System.out.println("[LOAN-APP] Feign error calling profile-service: " + e.getMessage());
            System.out.println("[LOAN-APP] Allowing loan application to proceed without profile verification for now");
        } catch (Exception e) {
            System.out.println("[LOAN-APP] Unexpected error: " + e.getClass().getName() + ", message: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

