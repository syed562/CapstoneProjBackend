package com.example.loanservice.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class Loan {
    private String id;
    @NotBlank
    private String userId;
    @NotNull @Min(1)
    private Double amount;
    @NotNull @Min(1)
    private Integer termMonths;
    private Double ratePercent;
    private String status; // pending | approved | rejected | active | closed
    private String createdAt;
    private String updatedAt;

}
