package com.example.loanservice.client.dto;

public class LoanApplicationView {
    private String id;
    private String userId;
    private Double amount;
    private Integer termMonths;
    private Double ratePercent;
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }

    public Double getRatePercent() { return ratePercent; }
    public void setRatePercent(Double ratePercent) { this.ratePercent = ratePercent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
