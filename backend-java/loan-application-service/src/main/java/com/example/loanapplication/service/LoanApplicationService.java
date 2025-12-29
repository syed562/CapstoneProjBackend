package com.example.loanapplication.service;

import com.example.loanapplication.domain.LoanApplication;
import com.example.loanapplication.domain.LoanApplicationRepository;
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
        app.setStatus("UNDER_REVIEW");
        app.setUpdatedAt(Instant.now().toString());
        return repo.save(app);
    }
}
