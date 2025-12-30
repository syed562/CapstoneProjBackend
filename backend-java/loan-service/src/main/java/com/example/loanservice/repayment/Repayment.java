package com.example.loanservice.repayment;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "repayments")
@Data
public class Repayment {
    @Id
    private String id;

    @Column(nullable = false)
    private String loanId;

    @Column(nullable = false)
    private String emiScheduleId;

    @Column(nullable = false)
    private Double amountPaid;

    @Column(nullable = false)
    private String paymentDate;

    @Column(nullable = false)
    private String paymentMethod; // BANK_TRANSFER, CHEQUE, CASH, ONLINE

    private String transactionId;

    @Column(nullable = false)
    private String status; // COMPLETED, FAILED, PENDING

    @Column(nullable = false)
    private String createdAt;
}
