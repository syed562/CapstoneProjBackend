package com.example.loanapplication.service;

import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.MODELS.LoanType;
import com.example.loanapplication.client.LoanServiceClient;
import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.dto.ProfileView;
import com.example.loanapplication.repository.LoanApplicationRepository;
import feign.FeignException;
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

@Service
public class LoanApplicationService {
    private final LoanApplicationRepository repo;
    private final ApprovalCriteriaService approvalCriteriaService;
    private final NotificationService notificationService;
    private final ProfileServiceClient profileServiceClient;
    private final LoanServiceClient loanServiceClient;
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
            @Value("${loan.rules.amount.min:5000}") double minAmount,
            @Value("${loan.rules.amount.max:2000000}") double maxAmount,
            @Value("${loan.rules.tenures:12,24,36}") String tenureOptions,
            @Value("${loan.rules.rates:PERSONAL=12,HOME=8.5,AUTO=10}") String rateOptions
    ) {
        this.repo = repo;
        this.approvalCriteriaService = approvalCriteriaService;
        this.notificationService = notificationService;
        this.profileServiceClient = profileServiceClient;
        this.loanServiceClient = loanServiceClient;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.allowedTenures = parseIntSet(tenureOptions);
        this.allowedTypes = Set.of(LoanType.values());
        this.defaultRates = parseRateMap(rateOptions);
    }

    public LoanApplication apply(String userId, LoanType loanType, double amount, int termMonths, Double ratePercent) {
        // TODO: Re-enable profile existence check once Feign integration is stable
        // ensureProfileExists(userId);
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
        return repo.save(app);
    }

    public List<LoanApplication> listByUser(String userId) {
        return repo.findByUserId(userId);
    }

    public LoanApplication get(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
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
        if (!"UNDER_REVIEW".equals(app.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application must be in UNDER_REVIEW status to approve");
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
            System.out.println("[LOAN-APP] Creating loan from approved application: " + saved.getId());
            System.out.println("[LOAN-APP] Loan Request - UserId: " + app.getUserId() + ", Amount: " + app.getAmount() + ", Type: " + app.getLoanType());
            loanServiceClient.createLoanFromApplication(loanRequest);
            System.out.println("[LOAN-APP] ✓ SUCCESS: Loan created in loan-service for application: " + saved.getId());
        } catch (Exception e) {
            System.err.println("[LOAN-APP] ✗ CRITICAL ERROR: Failed to create loan in loan-service for application: " + saved.getId());
            System.err.println("[LOAN-APP] Error Type: " + e.getClass().getName());
            System.err.println("[LOAN-APP] Error Message: " + e.getMessage());
            e.printStackTrace();
            // Log for debugging but don't fail - admin can manually retry
            System.err.println("[LOAN-APP] Application approved but loan creation failed. Manual retry needed.");
        }
        
        return saved;
    }

    public LoanApplication reject(String id, String remarks) {
        LoanApplication app = get(id);
        if (!"UNDER_REVIEW".equals(app.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application must be in UNDER_REVIEW status to reject");
        }
        if (remarks == null || remarks.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection remarks are required");
        }
        app.setStatus("REJECTED");
        app.setRemarks(remarks);
        app.setUpdatedAt(Instant.now().toString());
        LoanApplication saved = repo.save(app);
        
        // Send rejection notification
        notificationService.sendRejectionNotification(app.getUserId(), app.getId(), remarks);
        
        return saved;
    }

    public List<LoanApplication> listAll() {
        return repo.findAll();
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
        Double rate = defaultRates.get(loanType);
        if (rate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No default rate configured for loan type " + loanType);
        }
        return rate;
    }

    private void ensureProfileExists(String userId) {
        try {
            System.out.println("[LOAN-APP] Checking profile for userId: " + userId);
            ProfileView profile = profileServiceClient.getProfile(userId);
            System.out.println("[LOAN-APP] Profile found: " + profile);
            if (profile == null) {
                System.out.println("[LOAN-APP] WARNING: Profile is null for userId: " + userId + ", allowing application anyway");
                return;
            }
        } catch (FeignException.NotFound e) {
            System.out.println("[LOAN-APP] Profile not found (404): " + userId + ", error: " + e.getMessage() + ", allowing application anyway");
            return;
        } catch (FeignException e) {
            System.out.println("[LOAN-APP] Feign error calling profile-service: " + e.getClass().getName() + ", status: " + e.status() + ", message: " + e.getMessage() + ", allowing application anyway");
            return;
        } catch (Exception e) {
            System.out.println("[LOAN-APP] Unexpected error: " + e.getClass().getName() + ", message: " + e.getMessage() + ", allowing application anyway");
            e.printStackTrace();
            return;
        }
    }

}

