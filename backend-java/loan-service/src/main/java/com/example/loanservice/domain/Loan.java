package com.example.loanservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "loans")
@Data
public class Loan {
    @Id
    
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Integer termMonths;

    private Double ratePercent;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String createdAt;

    @Column(nullable = false)
    private String updatedAt;

   
}
