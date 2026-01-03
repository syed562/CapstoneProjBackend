package com.example.loanservice.service;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.Payment;
import com.example.loanservice.domain.LoanRepository;
import com.example.loanservice.emi.EMISchedule;
import com.example.loanservice.emi.EMIScheduleRepository;
import com.example.loanservice.event.EMIEvent;
import com.example.loanservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepo;
    private final LoanRepository loanRepo;
    private final EMIScheduleRepository emiRepo;
    private final NotificationPublisher notificationPublisher;

    public PaymentService(PaymentRepository paymentRepo, LoanRepository loanRepo, EMIScheduleRepository emiRepo,
                         NotificationPublisher notificationPublisher) {
        this.paymentRepo = paymentRepo;
        this.loanRepo = loanRepo;
        this.emiRepo = emiRepo;
        this.notificationPublisher = notificationPublisher;
    }

    /**
     * Record a payment for an EMI
     */
    public Payment recordPayment(String loanId, String emiId, Double amount, String paymentMethod, String transactionId) {
        // Validate loan exists
        Loan loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        // Validate EMI exists and belongs to this loan
        EMISchedule emi = emiRepo.findById(emiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EMI not found"));

        if (!emi.getLoanId().equals(loanId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EMI does not belong to this loan");
        }

        // Check if EMI is already paid
        if ("PAID".equals(emi.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EMI already paid");
        }

        // Validate payment amount matches EMI amount
        if (!amount.equals(emi.getEmiAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Payment amount (" + amount + ") does not match EMI amount (" + emi.getEmiAmount() + ")");
        }

        // Create payment record
        String now = Instant.now().toString();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setLoanId(loanId);
        payment.setEmiId(emiId);
        payment.setAmount(amount);
        payment.setPaymentDate(now);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("COMPLETED");
        payment.setTransactionId(transactionId);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        Payment savedPayment = paymentRepo.save(payment);

        // Mark EMI as PAID
        emi.setStatus("PAID");
        emi.setPaidDate(now);
        emiRepo.save(emi);

        // Update loan outstanding balance
        Double currentBalance = loan.getOutstandingBalance() != null ? loan.getOutstandingBalance() : loan.getAmount();
        Double newBalance = currentBalance - emi.getPrincipalAmount();
        loan.setOutstandingBalance(newBalance);
        loan.setUpdatedAt(now);

        // If balance is zero, mark loan as CLOSED and publish event
        if (newBalance <= 0.01) {  // Account for floating point precision
            loan.setStatus("CLOSED");
            publishLoanClosureEvent(loan);
        }
        loanRepo.save(loan);

        return savedPayment;
    }
    
    /**
     * Publish loan closure event when all EMIs are paid
     */
    private void publishLoanClosureEvent(Loan loan) {
        try {
            EMIEvent closureEvent = new EMIEvent();
            closureEvent.setLoanId(loan.getId());
            closureEvent.setUserId(loan.getUserId());
            closureEvent.setOutstandingBalance(0.0);
            closureEvent.setDueDate(LocalDate.now());
            
            notificationPublisher.publishLoanClosed(closureEvent);
        } catch (Exception e) {
            // Log but don't fail payment if notification fails
            System.err.println("Failed to publish loan closure event for loan " + loan.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Get all payments for a loan
     */
    public List<Payment> getPaymentsByLoan(String loanId) {
        return paymentRepo.findByLoanIdOrderByCreatedAtDesc(loanId);
    }

    /**
     * Get payment for specific EMI
     */
    public List<Payment> getPaymentsByEmi(String emiId) {
        return paymentRepo.findByEmiId(emiId);
    }

    /**
     * Get payment by ID
     */
    public Payment getPayment(String paymentId) {
        return paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }
}
