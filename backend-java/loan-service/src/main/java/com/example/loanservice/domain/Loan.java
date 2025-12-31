package com.example.loanservice.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "loans")
@Data
public class Loan {
    @Id
    @Column(name = "id")
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    private Double ratePercent;

    @Column(nullable = false)
    private String status;

    private Double outstandingBalance;  // Remaining principal after payments

    @Column(nullable = false)
    private String createdAt;

    @Column(nullable = false)
    private String updatedAt;

   
}
