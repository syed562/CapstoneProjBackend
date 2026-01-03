package com.example.reportservice.service;

import com.example.reportservice.client.LoanClient;
import com.example.reportservice.client.LoanApplicationClient;
import com.example.reportservice.client.dto.LoanDTO;
import com.example.reportservice.client.dto.LoanApplicationDTO;
import com.example.reportservice.dto.CustomerLoanSummaryDTO;
import com.example.reportservice.dto.LoanStatusReportDTO;
import com.example.reportservice.controller.dto.ComprehensiveReportDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ReportService Tests")
class ReportServiceTest {

    @Mock
    private LoanClient loanClient;

    @Mock
    private LoanApplicationClient loanApplicationClient;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportService = new ReportService(loanClient, loanApplicationClient);
    }

    @Test
    @DisplayName("Should generate loan status report with correct distribution")
    void testGetLoanStatusReport() {
        // Arrange
        LoanDTO loan1 = new LoanDTO();
        loan1.setId("loan1");
        loan1.setStatus("APPROVED");
        loan1.setAmount(100000.0);

        LoanDTO loan2 = new LoanDTO();
        loan2.setId("loan2");
        loan2.setStatus("APPROVED");
        loan2.setAmount(50000.0);

        LoanDTO loan3 = new LoanDTO();
        loan3.setId("loan3");
        loan3.setStatus("CLOSED");
        loan3.setAmount(75000.0);

        when(loanClient.getAllLoans()).thenReturn(List.of(loan1, loan2, loan3));

        // Act
        LoanStatusReportDTO report = reportService.getLoanStatusReport();

        // Assert
        assertNotNull(report);
        assertEquals(3L, report.getTotalLoans());
        assertEquals(2L, report.getActiveLoanCount());
        assertEquals(1L, report.getClosedLoanCount());
        assertTrue(report.getStatusDistribution().containsKey("APPROVED"));
        verify(loanClient, times(1)).getAllLoans();
    }

    @Test
    @DisplayName("Should handle empty loan list")
    void testGetLoanStatusReportEmptyList() {
        // Arrange
        when(loanClient.getAllLoans()).thenReturn(List.of());

        // Act
        LoanStatusReportDTO report = reportService.getLoanStatusReport();

        // Assert
        assertEquals(0L, report.getTotalLoans());
        assertEquals(0L, report.getActiveLoanCount());
        assertEquals(0L, report.getClosedLoanCount());
    }

    @Test
    @DisplayName("Should generate customer loan summary with aggregations")
    void testGetCustomerLoanSummary() {
        // Arrange
        LoanDTO loan1 = new LoanDTO();
        loan1.setUserId("user1");
        loan1.setAmount(100000.0);
        loan1.setStatus("APPROVED");

        LoanDTO loan2 = new LoanDTO();
        loan2.setUserId("user1");
        loan2.setAmount(50000.0);
        loan2.setStatus("APPROVED");

        LoanDTO loan3 = new LoanDTO();
        loan3.setUserId("user2");
        loan3.setAmount(75000.0);
        loan3.setStatus("CLOSED");

        when(loanClient.getAllLoans()).thenReturn(List.of(loan1, loan2, loan3));

        // Act
        List<CustomerLoanSummaryDTO> summaries = reportService.getCustomerLoanSummary();

        // Assert
        assertNotNull(summaries);
        assertEquals(2, summaries.size());
        
        CustomerLoanSummaryDTO user1Summary = summaries.stream()
                .filter(s -> "user1".equals(s.getUserId()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(user1Summary);
        assertEquals(2L, user1Summary.getTotalLoans());
        assertEquals(150000.0, user1Summary.getTotalLoanAmount());
        assertEquals(2L, user1Summary.getActiveLoans());
    }

    @Test
    @DisplayName("Should generate dashboard statistics with correct calculations")
    void testGetDashboardStatistics() {
        // Arrange
        LoanDTO loan1 = new LoanDTO();
        loan1.setStatus("PENDING");
        loan1.setAmount(50000.0);

        LoanDTO loan2 = new LoanDTO();
        loan2.setStatus("APPROVED");
        loan2.setAmount(100000.0);

        LoanDTO loan3 = new LoanDTO();
        loan3.setStatus("APPROVED");
        loan3.setAmount(75000.0);

        LoanDTO loan4 = new LoanDTO();
        loan4.setStatus("REJECTED");
        loan4.setAmount(25000.0);

        when(loanClient.getAllLoans()).thenReturn(List.of(loan1, loan2, loan3, loan4));

        // Act
        Map<String, Object> stats = reportService.getDashboardStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(4L, stats.get("totalLoans"));
        assertEquals(1L, stats.get("pendingLoans"));
        assertEquals(2L, stats.get("approvedLoans"));
        assertEquals(1L, stats.get("rejectedLoans"));
        assertEquals(250000.0, (Double) stats.get("totalLoanAmount"), 0.01);
        assertEquals(175000.0, (Double) stats.get("approvedLoanAmount"), 0.01);
        assertEquals(50.0, (Double) stats.get("approvalRate"), 0.01);
    }

    @Test
    @DisplayName("Should calculate 0% approval rate when no loans")
    void testDashboardStatisticsZeroLoans() {
        // Arrange
        when(loanClient.getAllLoans()).thenReturn(List.of());

        // Act
        Map<String, Object> stats = reportService.getDashboardStatistics();

        // Assert
        assertEquals(0L, stats.get("totalLoans"));
        assertEquals(0.0, (Double) stats.get("approvalRate"), 0.01);
    }

    @Test
    @DisplayName("Should generate comprehensive report with applications and loans")
    void testGetComprehensiveReport() {
        // Arrange
        LoanApplicationDTO app1 = new LoanApplicationDTO();
        app1.setId("app1");
        app1.setStatus("PENDING");

        LoanApplicationDTO app2 = new LoanApplicationDTO();
        app2.setId("app2");
        app2.setStatus("APPROVED");

        LoanDTO loan1 = new LoanDTO();
        loan1.setId("loan1");
        loan1.setStatus("APPROVED");
        loan1.setAmount(100000.0);

        when(loanApplicationClient.getAllApplications()).thenReturn(List.of(app1, app2));
        when(loanClient.getAllLoans()).thenReturn(List.of(loan1));

        // Act
        ComprehensiveReportDTO report = reportService.getComprehensiveReport();

        // Assert
        assertNotNull(report);
        assertNotNull(report.getApplications());
        assertEquals(2L, report.getApplications().getTotal());
        verify(loanApplicationClient, times(1)).getAllApplications();
        verify(loanClient, times(1)).getAllLoans();
    }

    @Test
    @DisplayName("Should use Java Streams for grouping and filtering")
    void testStreamsUsage() {
        // Arrange
        LoanDTO loan1 = new LoanDTO();
        loan1.setUserId("user1");
        loan1.setStatus("APPROVED");
        loan1.setAmount(50000.0);

        LoanDTO loan2 = new LoanDTO();
        loan2.setUserId("user1");
        loan2.setStatus("CLOSED");
        loan2.setAmount(30000.0);

        when(loanClient.getAllLoans()).thenReturn(List.of(loan1, loan2));

        // Act
        List<CustomerLoanSummaryDTO> summaries = reportService.getCustomerLoanSummary();

        // Assert - Verify streams correctly grouped and filtered
        assertEquals(1, summaries.size());
        CustomerLoanSummaryDTO summary = summaries.get(0);
        assertEquals(2L, summary.getTotalLoans());
        assertEquals(80000.0, summary.getTotalLoanAmount());
        assertEquals(1L, summary.getActiveLoans()); // Only APPROVED counted
        assertEquals(50000.0, summary.getOutstandingAmount());
    }

    @Test
    @DisplayName("Should handle fallback when loan client returns empty")
    void testFallbackBehavior() {
        // Arrange
        when(loanClient.getAllLoans()).thenReturn(List.of()); // Fallback returns empty list

        // Act
        LoanStatusReportDTO report = reportService.getLoanStatusReport();

        // Assert - Should not throw exception
        assertNotNull(report);
        assertEquals(0L, report.getTotalLoans());
    }

    @Test
    @DisplayName("Should correctly calculate status distribution with Collectors.groupingBy")
    void testStatusDistributionCalculation() {
        // Arrange
        LoanDTO loan1 = new LoanDTO();
        loan1.setStatus("APPROVED");

        LoanDTO loan2 = new LoanDTO();
        loan2.setStatus("APPROVED");

        LoanDTO loan3 = new LoanDTO();
        loan3.setStatus("PENDING");

        LoanDTO loan4 = new LoanDTO();
        loan4.setStatus("REJECTED");

        when(loanClient.getAllLoans()).thenReturn(List.of(loan1, loan2, loan3, loan4));

        // Act
        LoanStatusReportDTO report = reportService.getLoanStatusReport();

        // Assert
        Map<String, Long> distribution = report.getStatusDistribution();
        assertEquals(2L, distribution.get("APPROVED"));
        assertEquals(1L, distribution.get("PENDING"));
        assertEquals(1L, distribution.get("REJECTED"));
    }
}
