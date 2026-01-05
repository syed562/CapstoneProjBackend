package com.example.loanservice.repayment;

import com.example.loanservice.repayment.dto.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans/{loanId}/repayments")
public class RepaymentController {
    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    @PostMapping
    public ResponseEntity<Repayment> recordPayment(
            @PathVariable String loanId,
            @Valid @RequestBody PaymentRequest request
    ) {
        Repayment repayment = repaymentService.recordPayment(
                loanId,
                request.getEmiScheduleId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getTransactionId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(repayment);
    }

    @GetMapping
    public ResponseEntity<List<Repayment>> getRepayments(@PathVariable String loanId) {
        return ResponseEntity.ok(repaymentService.getRepaymentsByLoan(loanId));
    }

    @GetMapping("/outstanding-balance")
    public ResponseEntity<?> getOutstandingBalance(@PathVariable String loanId) {
        double balance = repaymentService.getOutstandingBalance(loanId);
        return ResponseEntity.ok(java.util.Map.of("loanId", loanId, "outstandingBalance", balance));
    }

    @GetMapping("/completed-count")
    public ResponseEntity<?> getCompletedPayments(@PathVariable String loanId) {
        long count = repaymentService.getCompletedPayments(loanId);
        return ResponseEntity.ok(java.util.Map.of("loanId", loanId, "completedPayments", count));
    }
}
