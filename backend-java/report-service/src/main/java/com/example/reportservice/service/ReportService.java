package com.example.reportservice.service;

import com.example.reportservice.client.LoanClient;
import com.example.reportservice.client.dto.LoanDTO;
import com.example.reportservice.controller.dto.CustomerLoanSummaryDTO;
import com.example.reportservice.controller.dto.LoanStatusReportDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final LoanClient loanClient;

    public ReportService(LoanClient loanClient) {
        this.loanClient = loanClient;
    }

    /**
     * Get loan status distribution report
     * Uses Java Streams for grouping and counting
     */
    public LoanStatusReportDTO getLoanStatusReport() {
        List<LoanDTO> loans = loanClient.getAllLoans();

        // Group by status and count
        Map<String, Long> statusDistribution = loans.stream()
                .collect(Collectors.groupingBy(
                        LoanDTO::getStatus,
                        Collectors.counting()
                ));

        long activeCount = loans.stream()
                .filter(l -> "approved".equalsIgnoreCase(l.getStatus()))
                .count();

        long closedCount = loans.stream()
                .filter(l -> "closed".equalsIgnoreCase(l.getStatus()))
                .count();

        LoanStatusReportDTO report = new LoanStatusReportDTO();
        report.setStatusDistribution(statusDistribution);
        report.setTotalLoans((long) loans.size());
        report.setActiveLoanCount(activeCount);
        report.setClosedLoanCount(closedCount);

        return report;
    }

    /**
     * Get customer-wise loan summary
     * Groups loans by userId and calculates aggregate metrics
     */
    public List<CustomerLoanSummaryDTO> getCustomerLoanSummary() {
        List<LoanDTO> loans = loanClient.getAllLoans();

        return loans.stream()
                .collect(Collectors.groupingBy(LoanDTO::getUserId))
                .entrySet()
                .stream()
                .map(entry -> {
                    String userId = entry.getKey();
                    List<LoanDTO> userLoans = entry.getValue();

                    CustomerLoanSummaryDTO summary = new CustomerLoanSummaryDTO();
                    summary.setUserId(userId);
                    summary.setTotalLoans((long) userLoans.size());
                    summary.setTotalLoanAmount(userLoans.stream()
                            .mapToDouble(LoanDTO::getAmount)
                            .sum());
                    summary.setActiveLoans(userLoans.stream()
                            .filter(l -> "approved".equalsIgnoreCase(l.getStatus()))
                            .count());
                    summary.setOutstandingAmount(userLoans.stream()
                            .filter(l -> "approved".equalsIgnoreCase(l.getStatus()))
                            .mapToDouble(LoanDTO::getAmount)
                            .sum());

                    return summary;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStatistics() {
        List<LoanDTO> loans = loanClient.getAllLoans();

        long totalLoans = loans.size();
        long pendingLoans = loans.stream()
                .filter(l -> "pending".equalsIgnoreCase(l.getStatus()))
                .count();
        long approvedLoans = loans.stream()
                .filter(l -> "approved".equalsIgnoreCase(l.getStatus()))
                .count();
        long rejectedLoans = loans.stream()
                .filter(l -> "rejected".equalsIgnoreCase(l.getStatus()))
                .count();

        double totalLoanAmount = loans.stream()
                .mapToDouble(LoanDTO::getAmount)
                .sum();

        double approvedLoanAmount = loans.stream()
                .filter(l -> "approved".equalsIgnoreCase(l.getStatus()))
                .mapToDouble(LoanDTO::getAmount)
                .sum();

        return Map.ofEntries(
                Map.entry("totalLoans", totalLoans),
                Map.entry("pendingLoans", pendingLoans),
                Map.entry("approvedLoans", approvedLoans),
                Map.entry("rejectedLoans", rejectedLoans),
                Map.entry("totalLoanAmount", totalLoanAmount),
                Map.entry("approvedLoanAmount", approvedLoanAmount),
                Map.entry("approvalRate", totalLoans > 0 ? (double) approvedLoans / totalLoans * 100 : 0)
        );
    }
}
