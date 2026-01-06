package com.example.loanservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    // Explicit accessors to keep tests compiling even if Lombok processing is skipped
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getRatePercent() { return ratePercent; }
    public void setRatePercent(Double ratePercent) { this.ratePercent = ratePercent; }

   
}
