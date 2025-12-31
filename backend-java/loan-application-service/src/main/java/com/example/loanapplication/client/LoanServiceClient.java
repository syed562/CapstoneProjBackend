package com.example.loanapplication.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "loan-service", url = "http://localhost:8085")
public interface LoanServiceClient {
    
    @GetMapping("/api/loans/by-application/{applicationId}")
    LoanView getLoanByApplicationId(@PathVariable("applicationId") String applicationId);
    
    @PostMapping("/api/loans/{applicationId}/approve")
    LoanView createLoanFromApplication(@PathVariable("applicationId") String applicationId);
    
    public static class LoanView {
        public String id;
        public String userId;
        public Double amount;
        public Integer termMonths;
        public Double ratePercent;
        public String status;
    }
}
