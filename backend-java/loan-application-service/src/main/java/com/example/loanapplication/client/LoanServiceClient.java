package com.example.loanapplication.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "loan-service", fallback = LoanServiceClientFallback.class)
public interface LoanServiceClient {
    
    
    @PostMapping("/api/loans/from-application")
    void createLoanFromApplication(@RequestBody CreateLoanRequest request);
    
    
    class CreateLoanRequest {
        private String userId;
        private Double amount;
        private Integer termMonths;
        private Double ratePercent;
        private String loanType;
        
        public CreateLoanRequest() {}
        
        public CreateLoanRequest(String userId, Double amount, Integer termMonths, 
                               Double ratePercent, String loanType) {
            this.userId = userId;
            this.amount = amount;
            this.termMonths = termMonths;
            this.ratePercent = ratePercent;
            this.loanType = loanType;
        }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public Integer getTermMonths() { return termMonths; }
        public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
        
        public Double getRatePercent() { return ratePercent; }
        public void setRatePercent(Double ratePercent) { this.ratePercent = ratePercent; }
        
        public String getLoanType() { return loanType; }
        public void setLoanType(String loanType) { this.loanType = loanType; }
    }
}
