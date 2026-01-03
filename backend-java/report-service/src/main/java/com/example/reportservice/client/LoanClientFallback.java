package com.example.reportservice.client;

import com.example.reportservice.client.dto.LoanDTO;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;

/**
 * Fallback handler for LoanClient in report-service
 * Called when loan-service is unavailable
 */
@Component
@Slf4j
public class LoanClientFallback implements LoanClient {

    @Override
    public List<LoanDTO> getAllLoans() {
        log.warn("⚠️ FALLBACK: Loan Service is unavailable");
        log.warn("⚠️ FALLBACK: Cannot fetch loans - returning empty list");
        
        // Return empty list so reports can gracefully degrade
        return Collections.emptyList();
    }
}
