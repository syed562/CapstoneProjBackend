package com.example.loanservice.controller;

import com.example.loanservice.service.LoanService;
import com.example.loanservice.controller.dto.CreateLoanRequest;
import com.example.loanservice.controller.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.loanservice.domain.Loan;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin
public class LoanController {
    private final LoanService loans;

    public LoanController(LoanService loans) {
        this.loans = loans;
    }

    @GetMapping
    public List<Loan> list() { return loans.list(); }

    @GetMapping("/my")
    public List<Loan> myLoans(@RequestParam(name = "userId", required = false) String userId) {
        return loans.listByUser(userId);
    }

    @GetMapping("/{id}")
    public Loan get(@PathVariable(name="id") String id) {
        return loans.get(id);
    }

    @PostMapping
    public Loan create(@Valid @RequestBody CreateLoanRequest req) {
        return loans.create(req.getUserId(), req.getAmount(), req.getTermMonths(), req.getRatePercent());
    }

    @PatchMapping("/{id}/status")
    public Loan updateStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateStatusRequest req
    ) {
        return loans.updateStatus(id, req.getStatus());
    }

    @PostMapping("/{applicationId}/approve")
    public Loan approve(@PathVariable("applicationId") String applicationId) {
        return loans.approveFromApplication(applicationId);
    }

    @PostMapping("/{applicationId}/reject")
    public Loan reject(@PathVariable("applicationId") String applicationId) {
        // Not implemented yet: update application status in loan-application-service to REJECTED
        throw new UnsupportedOperationException("Reject by application not implemented");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        loans.delete(id);
        return ResponseEntity.noContent().build();
    }
}
