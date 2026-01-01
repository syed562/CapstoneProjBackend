package com.example.reportservice.controller.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ComprehensiveReportDTO {
    private ApplicationStatistics applications;
    private LoanStatistics loans;
    private OverallMetrics overall;

    @Data
    public static class ApplicationStatistics {
        private Long total;
        private Long pending;
        private Long underReview;
        private Long approved;
        private Long rejected;
        private Map<String, Long> statusDistribution;
    }

    @Data
    public static class LoanStatistics {
        private Long total;
        private Long pending;
        private Long approved;
        private Long closed;
        private Double totalAmount;
        private Double approvedAmount;
        private Map<String, Long> statusDistribution;
    }

    @Data
    public static class OverallMetrics {
        private Double applicationToLoanConversionRate;
        private Long totalCustomers;
        private Double averageLoanAmount;
    }
}
