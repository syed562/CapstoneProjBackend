package com.example.loanapplication.MODELS;

import jakarta.persistence.*;
import lombok.Data;
import com.example.loanapplication.security.EncryptedStringConverter;

@Entity
@Table(name = "loan_applications")
@Data
public class LoanApplication {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Transient
    private String userName;  // Will be populated by service layer

    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String amount;  // Encrypted: loan amount

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Convert(converter = EncryptedStringConverter.class)
    private String ratePercent;  // Encrypted: interest rate

    @Column(nullable = false)
    private String status; // SUBMITTED | UNDER_REVIEW | REJECTED | APPROVED | CLOSED

    private String remarks;

    @Column(nullable = false)
    private String createdAt;

    @Column(nullable = false)
    private String updatedAt;
}

