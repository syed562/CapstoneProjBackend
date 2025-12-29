package com.example.loanservice.service;

import com.example.loanservice.domain.Loan;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanService {
    private final List<Loan> loans = new ArrayList<>();

    public List<Loan> list() {
        return loans;
    }

    public Loan get(String id) {
        return findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
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
        loans.add(loan);
        return loan;
    }

    public Loan updateStatus(String id, String status) {
        Loan loan = get(id);
        loan.setStatus(status);
        loan.setUpdatedAt(Instant.now().toString());
        return loan;
    }

    public void delete(String id) {
        Loan loan = get(id);
        loans.remove(loan);
    }

    private Optional<Loan> findById(String id) {
        return loans.stream().filter(l -> id.equals(l.getId())).findFirst();
    }
}
