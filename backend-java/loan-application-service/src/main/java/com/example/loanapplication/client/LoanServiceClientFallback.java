package com.example.loanapplication.client;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback handler for LoanServiceClient
 * Called when loan-service is unavailable
 */
@Component
@Slf4j
public class LoanServiceClientFallback implements LoanServiceClient {

    @Override
    public void createLoanFromApplication(CreateLoanRequest request) {
        log.warn("⚠️ FALLBACK: Loan Service is unavailable. Loan creation failed for userId: {}, amount: {}", 
                request.getUserId(), request.getAmount());
        log.warn("⚠️ FALLBACK: Application is approved but loan record was NOT created in loan-service");
        log.warn("⚠️ FALLBACK: Manual intervention required. Admin should retry loan creation via API.");
        
        // In production, you could:
        // 1. Store in a retry queue/table
        // 2. Send alert to admin
        // 3. Log to external monitoring service
        
        // For now, just log - the exception will be caught in LoanApplicationService.approve()
    }
}
