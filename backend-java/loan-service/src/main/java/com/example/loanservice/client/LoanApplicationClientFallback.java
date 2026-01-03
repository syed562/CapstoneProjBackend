package com.example.loanservice.client;

import com.example.loanservice.client.dto.LoanApplicationView;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback handler for LoanApplicationClient in loan-service
 * Called when loan-application-service is unavailable
 */
@Component
@Slf4j
public class LoanApplicationClientFallback implements LoanApplicationClient {

    @Override
    public LoanApplicationView getApplication(String applicationId) {
        log.warn("⚠️ FALLBACK: Loan Application Service is unavailable for applicationId: {}", applicationId);
        log.warn("⚠️ FALLBACK: Cannot fetch application details - returning null");
        
        // Return null - caller should handle gracefully
        return null;
    }
}
