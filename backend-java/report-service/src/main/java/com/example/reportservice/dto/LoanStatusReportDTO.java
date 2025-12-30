package com.example.reportservice.controller.dto;

import lombok.Data;
import java.util.Map;

@Data
public class LoanStatusReportDTO {
    private Map<String, Long> statusDistribution;
    private Long totalLoans;
    private Long activeLoanCount;
    private Long closedLoanCount;
}
