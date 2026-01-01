package com.example.loanservice.controller;

import com.example.loanservice.domain.Payment;
import com.example.loanservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Record a payment for an EMI
     */
    @PostMapping("/record")
    public ResponseEntity<Payment> recordPayment(@Valid @RequestBody RecordPaymentRequest req) {
        Payment payment = paymentService.recordPayment(
                req.getLoanId(),
                req.getEmiId(),
                req.getAmount(),
                req.getPaymentMethod(),
                req.getTransactionId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * Get all payments for a loan
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<Payment>> getPaymentsByLoan(@PathVariable("loanId") String loanId) {
        return ResponseEntity.ok(paymentService.getPaymentsByLoan(loanId));
    }

    /**
     * Get payment for specific EMI
     */
    @GetMapping("/emi/{emiId}")
    public ResponseEntity<List<Payment>> getPaymentsByEmi(@PathVariable String emiId) {
        return ResponseEntity.ok(paymentService.getPaymentsByEmi(emiId));
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
