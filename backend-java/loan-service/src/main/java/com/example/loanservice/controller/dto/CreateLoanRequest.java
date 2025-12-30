package com.example.loanservice.controller.dto;

import com.example.loanservice.domain.LoanType;
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
    @NotNull @Min(1)
    private Integer termMonths;
    @NotNull
    private LoanType loanType;
    private Double ratePercent;

 
}