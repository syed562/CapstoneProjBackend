package com.example.loanservice.service;

import com.example.loanservice.client.LoanApplicationClient;
import com.example.loanservice.client.dto.LoanApplicationView;
import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import com.example.loanservice.domain.LoanType;
import com.example.loanservice.emi.EMIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Service
public class LoanService {
    private final LoanRepository repo;
    private final LoanApplicationClient loanApplicationClient;
    private final EMIService emiService;
    private final LoanNotificationService notificationService;
    private final Map<LoanType, Double> defaultRates;

    @PersistenceContext
    private EntityManager entityManager;

    public LoanService(LoanRepository repo, LoanApplicationClient loanApplicationClient,
                     EMIService emiService, LoanNotificationService notificationService,
                     @Value("${loan.rules.rates:PERSONAL=12,HOME=8.5,AUTO=10}") String rateOptions) {
        this.repo = repo;
        this.loanApplicationClient = loanApplicationClient;
        this.emiService = emiService;
        this.notificationService = notificationService;
        this.defaultRates = parseRateMap(rateOptions);
    }

    public List<Loan> list() {
        return repo.findAll();
    }

    public Page<Loan> listPaged(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.findAll(pageable);
    }

    public List<Loan> listByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return repo.findByUserId(userId);
    }

    public List<Loan> findByStatusAndAmount(String status, Double minAmount, Double maxAmount) {
        return repo.findByStatusAndAmountRange(status, minAmount, maxAmount);
    }

    public List<Loan> searchCriteria(String userId, String status, Double minAmount, Double maxAmount) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Loan> query = cb.createQuery(Loan.class);
        Root<Loan> root = query.from(Loan.class);

        Predicate predicate = cb.conjunction();
        if (userId != null && !userId.isBlank()) {
            predicate = cb.and(predicate, cb.equal(root.get("userId"), userId));
        }
        if (status != null && !status.isBlank()) {
            predicate = cb.and(predicate, cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
        }
        if (minAmount != null) {
            predicate = cb.and(predicate, cb.ge(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            predicate = cb.and(predicate, cb.le(root.get("amount"), maxAmount));
        }

        query.select(root)
             .where(predicate)
             .orderBy(cb.desc(root.get("createdAt")));

        return entityManager.createQuery(query).getResultList();
    }

    public Loan get(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    public Loan create(String userId, LoanType loanType, double amount, int termMonths, Double ratePercent) {
        String now = Instant.now().toString();
        double resolvedRate = ratePercent != null ? ratePercent : resolveRateForType(loanType);
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserId(userId);
        loan.setAmount(amount);
        loan.setLoanType(loanType);
        loan.setTermMonths(termMonths);
        loan.setRatePercent(resolvedRate);
        loan.setStatus("pending");
        loan.setOutstandingBalance(amount);  // Initialize with full loan amount
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
        return repo.save(loan);
    }

    /**
     * Create loan from approved application
     * This method is called by LoanApplicationService after approving an application
     * LoanService is responsible only for creating the loan, not approving
     */
    @Transactional
    public void createLoanFromApplication(com.example.loanservice.controller.dto.CreateLoanRequest req) {
        System.out.println("[LOAN-SERVICE] Creating loan from approved application data");
        System.out.println("[LOAN-SERVICE] Loan Request - UserId: " + req.getUserId() + ", Amount: " + req.getAmount() + ", Type: " + req.getLoanType());
        
        String now = Instant.now().toString();
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserId(req.getUserId());
        loan.setAmount(req.getAmount());
        loan.setLoanType(req.getLoanType());
        loan.setTermMonths(req.getTermMonths());
        Double resolvedRate = req.getRatePercent() != null ? req.getRatePercent() : resolveRateForType(req.getLoanType());
        loan.setRatePercent(resolvedRate);
        loan.setStatus("approved");
        loan.setOutstandingBalance(req.getAmount());
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
        
        System.out.println("[LOAN-SERVICE] Saving loan to database - Amount: " + loan.getAmount() + ", Type: " + loan.getLoanType() + ", TermMonths: " + loan.getTermMonths());
        Loan saved = repo.save(loan);
        System.out.println("[LOAN-SERVICE] ✓ Loan saved to database with ID: " + saved.getId());

        // Auto-generate EMI schedule after approval
        System.out.println("[LOAN-SERVICE] Generating EMI schedule for loan: " + saved.getId());
        try {
            emiService.generateEMISchedule(saved.getId());
            System.out.println("[LOAN-SERVICE] ✓ EMI schedule generated successfully");
        } catch (Exception e) {
            System.err.println("[LOAN-SERVICE] ✗ EMI generation failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Send EMI notification to customer
        System.out.println("[LOAN-SERVICE] Sending EMI notification to customer: " + req.getUserId());
        try {
            notificationService.sendEMINotification(req.getUserId(), saved.getId(),
                req.getAmount(), resolvedRate, req.getTermMonths());
            System.out.println("[LOAN-SERVICE] ✓ EMI notification sent");
        } catch (Exception e) {
            System.err.println("[LOAN-SERVICE] ✗ EMI notification failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[LOAN-SERVICE] ✓ Loan creation complete from application request");
    }

    public Loan approveFromApplication(String applicationId) {
        LoanApplicationView app = loanApplicationClient.getApplication(applicationId);
        if (app == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }
        String now = Instant.now().toString();
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserId(app.getUserId());
        loan.setAmount(app.getAmount());
        try {
            loan.setLoanType(LoanType.valueOf(app.getLoanType().toUpperCase()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid loan type on application: " + app.getLoanType());
        }
        loan.setTermMonths(app.getTermMonths());
        Double resolvedRate = app.getRatePercent() != null ? app.getRatePercent() : resolveRateForType(loan.getLoanType());
        loan.setRatePercent(resolvedRate);
        loan.setStatus("approved");
        loan.setOutstandingBalance(app.getAmount());  // Initialize with full amount
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
        System.out.println("[LOAN-SERVICE] Creating loan from application: " + applicationId);
        System.out.println("[LOAN-SERVICE] Loan Details - Amount: " + loan.getAmount() + ", Type: " + loan.getLoanType() + ", Tenure: " + loan.getTermMonths());
        Loan saved = repo.save(loan);
        System.out.println("[LOAN-SERVICE] ✓ Loan saved to database with ID: " + saved.getId());

        // Auto-generate EMI schedule after approval
        System.out.println("[LOAN-SERVICE] Generating EMI schedule for loan: " + saved.getId());
        try {
            emiService.generateEMISchedule(saved.getId());
            System.out.println("[LOAN-SERVICE] ✓ EMI schedule generated successfully");
        } catch (Exception e) {
            System.err.println("[LOAN-SERVICE] ✗ EMI generation failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Send EMI notification to customer
        System.out.println("[LOAN-SERVICE] Sending EMI notification to customer: " + app.getUserId());
        try {
            notificationService.sendEMINotification(app.getUserId(), saved.getId(),
                app.getAmount(), resolvedRate, app.getTermMonths());
            System.out.println("[LOAN-SERVICE] ✓ EMI notification sent");
        } catch (Exception e) {
            System.err.println("[LOAN-SERVICE] ✗ EMI notification failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[LOAN-SERVICE] ✓ Loan approval complete for application: " + applicationId);
        return saved;
    }

    public Loan updateStatus(String id, String status) {
        Loan loan = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        loan.setStatus(status);
        loan.setUpdatedAt(Instant.now().toString());
        return repo.save(loan);
    }

    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found");
        }
        repo.deleteById(id);
    }

    public void generateEMIForLoan(String loanId) {
        if (!repo.existsById(loanId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found");
        }
        emiService.generateEMISchedule(loanId);
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
}
