package com.example.loanservice.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordPaymentRequest {
    @NotBlank
    private String loanId;

    @NotBlank
    private String emiId;

    @NotNull
    @Min(1)
    private Double amount;

    @NotBlank
    private String paymentMethod;  // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, CASH

    private String transactionId;  // Optional: external payment gateway reference
}
