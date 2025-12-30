package com.example.reportservice.client.dto;

import lombok.Data;

@Data
public class LoanDTO {
    private String id;
    private String userId;
    private Double amount;
    private Integer termMonths;
    private Double ratePercent;
    private String status;
    private String createdAt;
    private String updatedAt;
}
