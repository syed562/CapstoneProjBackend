package com.example.loanservice.service;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {
    private final LoanRepository repo;

    public LoanService(LoanRepository repo) {
        this.repo = repo;
    }

    public List<Loan> list() {
        return repo.findAll();
    }

    public List<Loan> listByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return repo.findByUserId(userId);
    }

    public Loan get(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    public Loan create(String userId, double amount, int termMonths, Double ratePercent) {
        String now = Instant.now().toString();
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserId(userId);
        loan.setAmount(amount);
        loan.setTermMonths(termMonths);
        loan.setRatePercent(ratePercent);
        loan.setStatus("pending");
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
        return repo.save(loan);
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
}
