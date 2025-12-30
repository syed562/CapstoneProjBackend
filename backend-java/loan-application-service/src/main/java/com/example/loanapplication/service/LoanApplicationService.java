package com.example.loanapplication.service;

import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.MODELS.LoanType;
import com.example.loanapplication.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LoanApplicationService {
    private final LoanApplicationRepository repo;
    private final double minAmount;
    private final double maxAmount;
    private final Set<Integer> allowedTenures;
    private final Set<LoanType> allowedTypes;

    public LoanApplicationService(
            LoanApplicationRepository repo,
            @Value("${loan.rules.amount.min:5000}") double minAmount,
            @Value("${loan.rules.amount.max:2000000}") double maxAmount,
            @Value("${loan.rules.tenures:12,24,36}") String tenureOptions
    ) {
        this.repo = repo;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.allowedTenures = parseIntSet(tenureOptions);
        this.allowedTypes = Set.of(LoanType.values());
    }

    public LoanApplication apply(String userId, LoanType loanType, double amount, int termMonths, Double ratePercent) {
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

        String now = Instant.now().toString();
        LoanApplication app = new LoanApplication();
        app.setId(UUID.randomUUID().toString());
        app.setUserId(userId);
        app.setLoanType(loanType);
        app.setAmount(amount);
        app.setTermMonths(termMonths);
        app.setRatePercent(ratePercent);
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
        app.setStatus("APPROVED");
        app.setUpdatedAt(Instant.now().toString());
        return repo.save(app);
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
        return repo.save(app);
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

}

