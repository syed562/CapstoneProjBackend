package com.example.loanapplication.client;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class LoanServiceClientFallback implements LoanServiceClient {

    @Override
    public void createLoanFromApplication(CreateLoanRequest request) {
        log.warn("⚠️ FALLBACK: Loan Service is unavailable. Loan creation failed for userId: {}, amount: {}", 
                request.getUserId(), request.getAmount());
        log.warn("⚠️ FALLBACK: Application is approved but loan record was NOT created in loan-service");
        log.warn("⚠️ FALLBACK: Manual intervention required. Admin should retry loan creation via API.");
        

    }
}
