package com.example.loanservice.emi;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "emi_schedules")
@Data
public class EMISchedule {
    @Id
    private String id;

    @Column(nullable = false)
    private String loanId;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Double emiAmount;

    @Column(nullable = false)
    private Double principalAmount;

    @Column(nullable = false)
    private Double interestAmount;

    @Column(nullable = false)
    private Double outstandingBalance;

    @Column(nullable = false)
    private String status; // SCHEDULED, PAID, OVERDUE

    private String paidDate;

    @Column(nullable = false)
    private String dueDate;

    @Column(nullable = false)
    private String createdAt;
}
