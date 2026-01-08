package com.example.loanservice.client.dto;

import lombok.Data;

@Data
public class ProfileView {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Double creditScore;
    private Double annualIncome;
    private Double totalLiabilities;
    private String kycStatus;
}
