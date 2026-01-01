package com.example.reportservice.controller;

import com.example.reportservice.controller.dto.ComprehensiveReportDTO;
import com.example.reportservice.controller.dto.CustomerLoanSummaryDTO;
import com.example.reportservice.controller.dto.LoanStatusReportDTO;
import com.example.reportservice.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/loan-status")
    public ResponseEntity<LoanStatusReportDTO> getLoanStatusReport() {
        return ResponseEntity.ok(reportService.getLoanStatusReport());
    }

    @GetMapping("/customer-summary")
    public ResponseEntity<List<CustomerLoanSummaryDTO>> getCustomerLoanSummary() {
        return ResponseEntity.ok(reportService.getCustomerLoanSummary());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(reportService.getDashboardStatistics());
    }

    @GetMapping("/comprehensive")
    public ResponseEntity<ComprehensiveReportDTO> getComprehensiveReport() {
        return ResponseEntity.ok(reportService.getComprehensiveReport());
    }
}
