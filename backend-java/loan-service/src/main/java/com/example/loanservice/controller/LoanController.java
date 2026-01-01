package com.example.loanservice.controller;

import com.example.loanservice.service.LoanService;
import com.example.loanservice.controller.dto.CreateLoanRequest;
import com.example.loanservice.controller.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/paged")
    public Page<Loan> listPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort
    ) {
        Sort sortObj = parseSort(sort);
        return loans.listPaged(page, size, sortObj);
    }

    @GetMapping("/my")
    public List<Loan> myLoans(@RequestParam(name = "userId", required = false) String userId) {
        return loans.listByUser(userId);
    }

    @GetMapping("/filter")
    public List<Loan> filterLoans(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount
    ) {
        return loans.findByStatusAndAmount(status, minAmount, maxAmount);
    }

    @GetMapping("/search")
    public List<Loan> searchLoans(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount
    ) {
        return loans.searchCriteria(userId, status, minAmount, maxAmount);
    }

    @GetMapping("/{id}")
    public Loan get(@PathVariable(name="id") String id) {
        return loans.get(id);
    }

    @PostMapping
    public Loan create(@Valid @RequestBody CreateLoanRequest req) {
        return loans.create(req.getUserId(), req.getLoanType(), req.getAmount(), req.getTermMonths(), req.getRatePercent());
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

    @PostMapping("/{id}/generate-emi")
    public ResponseEntity<String> generateEMI(@PathVariable("id") String id) {
        loans.generateEMIForLoan(id);
        return ResponseEntity.ok("EMI schedule generated for loan " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        loans.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1) {
            try {
                direction = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ignored) {
                direction = Sort.Direction.DESC;
            }
        }
        return Sort.by(direction, property);
    }
}
