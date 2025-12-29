package com.example.loanapplication.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "loan_applications")
@Data
public class LoanApplication {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

   
    private Double amount;

    @Column(nullable = false)
    private Integer termMonths;

    private Double ratePercent;

    @Column(nullable = false)
    private String status; // SUBMITTED | UNDER_REVIEW | REJECTED | APPROVED

    @Column(nullable = false)
    private String createdAt;

    @Column(nullable = false)
    private String updatedAt;
}
