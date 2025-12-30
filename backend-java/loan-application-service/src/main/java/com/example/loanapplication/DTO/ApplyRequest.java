package com.example.loanapplication.DTO;

import com.example.loanapplication.MODELS.LoanType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyRequest {
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
