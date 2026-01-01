package com.example.reportservice.client.dto;

import lombok.Data;

@Data
public class LoanApplicationDTO {
    private String id;
    private String userId;
    private String loanType;
    private String amount;
    private Integer termMonths;
    private String ratePercent;
    private String status;
    private String remarks;
    private String createdAt;
    private String updatedAt;
}
