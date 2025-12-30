package com.example.reportservice.controller.dto;

import lombok.Data;

@Data
public class CustomerLoanSummaryDTO {
    private String userId;
    private Long totalLoans;
    private Double totalLoanAmount;
    private Long activeLoans;
    private Double outstandingAmount;
}
