package com.example.reportservice.service;

import com.example.reportservice.client.LoanClient;
import com.example.reportservice.client.LoanApplicationClient;
import com.example.reportservice.client.dto.LoanDTO;
import com.example.reportservice.client.dto.LoanApplicationDTO;
import com.example.reportservice.dto.CustomerLoanSummaryDTO;
import com.example.reportservice.dto.LoanStatusReportDTO;
import com.example.reportservice.controller.dto.ComprehensiveReportDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final LoanClient loanClient;
    private final LoanApplicationClient loanApplicationClient;

    public ReportService(LoanClient loanClient, LoanApplicationClient loanApplicationClient) {
        this.loanClient = loanClient;
        this.loanApplicationClient = loanApplicationClient;
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

    /**
     * Get comprehensive report combining applications and loans
     */
    public ComprehensiveReportDTO getComprehensiveReport() {
        List<LoanApplicationDTO> applications = loanApplicationClient.getAllApplications();
        List<LoanDTO> loans = loanClient.getAllLoans();

        ComprehensiveReportDTO report = new ComprehensiveReportDTO();

        // Application Statistics
        ComprehensiveReportDTO.ApplicationStatistics appStats = new ComprehensiveReportDTO.ApplicationStatistics();
        appStats.setTotal((long) applications.size());
        
        Map<String, Long> appStatusDist = applications.stream()
                .collect(Collectors.groupingBy(
                        LoanApplicationDTO::getStatus,
                        Collectors.counting()
                ));
        appStats.setStatusDistribution(appStatusDist);
        
        appStats.setPending(applications.stream()
                .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()))
                .count());
        appStats.setUnderReview(applications.stream()
                .filter(a -> "UNDER_REVIEW".equalsIgnoreCase(a.getStatus()))
                .count());
        appStats.setApproved(applications.stream()
                .filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus()))
                .count());
        appStats.setRejected(applications.stream()
                .filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus()))
                .count());
        
        report.setApplications(appStats);

        // Loan Statistics
        ComprehensiveReportDTO.LoanStatistics loanStats = new ComprehensiveReportDTO.LoanStatistics();
        loanStats.setTotal((long) loans.size());
        
        Map<String, Long> loanStatusDist = loans.stream()
                .collect(Collectors.groupingBy(
                        LoanDTO::getStatus,
                        Collectors.counting()
                ));
        loanStats.setStatusDistribution(loanStatusDist);
        
        loanStats.setPending(loans.stream()
                .filter(l -> "pending".equalsIgnoreCase(l.getStatus()))
                .count());
        loanStats.setApproved(loans.stream()
                .filter(l -> "approved".equalsIgnoreCase(l.getStatus()))
                .count());
        loanStats.setClosed(loans.stream()
                .filter(l -> "closed".equalsIgnoreCase(l.getStatus()))
                .count());
        
        double totalLoanAmount = loans.stream()
                .mapToDouble(LoanDTO::getAmount)
                .sum();
        double approvedLoanAmount = loans.stream()
                .filter(l -> "approved".equalsIgnoreCase(l.getStatus()))
                .mapToDouble(LoanDTO::getAmount)
                .sum();
        
        loanStats.setTotalAmount(totalLoanAmount);
        loanStats.setApprovedAmount(approvedLoanAmount);
        
        report.setLoans(loanStats);

        // Overall Metrics
        ComprehensiveReportDTO.OverallMetrics overall = new ComprehensiveReportDTO.OverallMetrics();
        
        double conversionRate = appStats.getApproved() > 0 && loanStats.getTotal() > 0
                ? (double) loanStats.getTotal() / appStats.getApproved() * 100
                : 0;
        overall.setApplicationToLoanConversionRate(conversionRate);
        
        long uniqueCustomers = applications.stream()
                .map(LoanApplicationDTO::getUserId)
                .distinct()
                .count();
        overall.setTotalCustomers(uniqueCustomers);
        
        double avgLoanAmount = loanStats.getTotal() > 0 
                ? totalLoanAmount / loanStats.getTotal()
                : 0;
        overall.setAverageLoanAmount(avgLoanAmount);
        
        report.setOverall(overall);

        return report;
    }
}

