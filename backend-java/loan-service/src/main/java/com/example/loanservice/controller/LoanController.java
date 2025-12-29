package com.example.loanservice.controller;


import com.example.loanservice.service.LoanService;
import com.example.loanservice.controller.dto.CreateLoanRequest;
import com.example.loanservice.controller.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.loanservice.domain.Loan;

import java.util.List;

@RestController
@RequestMapping("/loans")
@CrossOrigin
public class LoanController {
    private final LoanService loans;

    public LoanController(LoanService loans) {
        this.loans = loans;
    }

    @GetMapping
    public List<Loan> list() { return loans.list(); }

    @GetMapping("/{id}")
    public Loan get(@PathVariable String id) {
         return loans.get(id); }

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


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        loans.delete(id);
        return ResponseEntity.noContent().build();
    }
}
