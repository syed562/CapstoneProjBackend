package com.example.loanapplication.service;

import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LoanApplicationService {
    private final LoanApplicationRepository repo;

    public LoanApplicationService(LoanApplicationRepository repo) {
        this.repo = repo;
    }

    public LoanApplication apply(String userId, double amount, int termMonths, Double ratePercent) {
        String now = Instant.now().toString();
        LoanApplication app = new LoanApplication();
        app.setId(UUID.randomUUID().toString());
        app.setUserId(userId);
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
        app.setStatus("REJECTED");
        app.setRemarks(remarks != null ? remarks : "Application rejected");
        app.setUpdatedAt(Instant.now().toString());
        return repo.save(app);
    }

    public List<LoanApplication> listAll() {
        return repo.findAll();
    }
}

