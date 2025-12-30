package com.example.loanapplication.controller;

import com.example.loanapplication.DTO.ApplyRequest;
import com.example.loanapplication.DTO.ApprovalRequest;
import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
@CrossOrigin
public class LoanApplicationController {
    private final LoanApplicationService apps;

    public LoanApplicationController(LoanApplicationService apps) {
        this.apps = apps;
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<LoanApplication> apply(@Valid @RequestBody ApplyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                apps.apply(req.getUserId(), req.getLoanType(), req.getAmount(), req.getTermMonths(), req.getRatePercent())
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('LOAN_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<List<LoanApplication>> listAll() {
        return ResponseEntity.ok(apps.listAll());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<LoanApplication>> my(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(apps.listByUser(userId));
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('LOAN_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<LoanApplication> get(@PathVariable("applicationId") String applicationId) {
        return ResponseEntity.ok(apps.get(applicationId));
    }

    @PutMapping("/{applicationId}/review")
    @PreAuthorize("hasRole('LOAN_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<LoanApplication> markUnderReview(@PathVariable("applicationId") String applicationId) {
        return ResponseEntity.ok(apps.markUnderReview(applicationId));
    }

    @PutMapping("/{applicationId}/approve")
    @PreAuthorize("hasRole('LOAN_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<LoanApplication> approve(@PathVariable("applicationId") String applicationId) {
        return ResponseEntity.ok(apps.approve(applicationId));
    }

    @PutMapping("/{applicationId}/reject")
    @PreAuthorize("hasRole('LOAN_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<LoanApplication> reject(
            @PathVariable("applicationId") String applicationId,
            @Valid @RequestBody ApprovalRequest request
    ) {
        return ResponseEntity.ok(apps.reject(applicationId, request.getRemarks()));
    }
}
