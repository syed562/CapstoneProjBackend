package com.example.loanservice.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    private String id;

    @Column(nullable = false)
    private String loanId;

    @Column(nullable = false)
    private String emiId;  // Which EMI this payment is for

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String paymentDate;  // When payment was made

    @Column(nullable = false)
    private String paymentMethod;  // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, CASH

    @Column(nullable = false)
    private String status;  // PENDING, COMPLETED, FAILED, REFUNDED

    private String transactionId;  // External transaction reference

    private String remarks;

    @Column(nullable = false)
    private String createdAt;

    @Column(nullable = false)
    private String updatedAt;
}
