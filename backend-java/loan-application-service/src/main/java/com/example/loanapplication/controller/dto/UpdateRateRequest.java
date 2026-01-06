package com.example.loanapplication.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateRateRequest {
    @NotBlank(message = "Loan type is required")
    private String loanType;

    @Positive(message = "Rate must be positive")
    private Double rate;
}
