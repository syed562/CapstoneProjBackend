package com.example.loanservice.controller.dto;

import com.example.loanservice.validation.AllowedTenure;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLoanRequest {
    @NotBlank
    private String userId;
    @NotNull @Min(1)
    private Double amount;
    @NotNull @Min(1) @AllowedTenure
    private Integer termMonths;
    @NotBlank
    private String loanType;
    private Double ratePercent;

    // Constructor with all fields
    public CreateLoanRequest() {}

    public CreateLoanRequest(String userId, Double amount, Integer termMonths, Double ratePercent, String loanType) {
        this.userId = userId;
        this.amount = amount;
        this.termMonths = termMonths;
        this.ratePercent = ratePercent;
        this.loanType = loanType;
    }

    // Explicit accessors to ensure availability even if Lombok processing is skipped during tests
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public Double getRatePercent() { return ratePercent; }
    public void setRatePercent(Double ratePercent) { this.ratePercent = ratePercent; }
}