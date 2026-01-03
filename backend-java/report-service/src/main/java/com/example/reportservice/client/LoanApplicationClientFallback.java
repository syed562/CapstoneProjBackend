package com.example.reportservice.client;

import com.example.reportservice.client.dto.LoanApplicationDTO;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;

/**
 * Fallback handler for LoanApplicationClient in report-service
 * Called when loan-application-service is unavailable
 */
@Component
@Slf4j
public class LoanApplicationClientFallback implements LoanApplicationClient {

    @Override
    public List<LoanApplicationDTO> getAllApplications() {
        log.warn("⚠️ FALLBACK: Loan Application Service is unavailable");
        log.warn("⚠️ FALLBACK: Cannot fetch applications - returning empty list");
        
        // Return empty list so reports can gracefully degrade
        return Collections.emptyList();
    }
}
