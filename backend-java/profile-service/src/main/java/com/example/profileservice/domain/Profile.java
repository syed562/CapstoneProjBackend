package com.example.profileservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
@Entity
@Table(name = "profiles")
@Data
public class Profile {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String kycStatus;
    private Double creditScore;     // Credit score (0-900)
    private Double annualIncome;    // Annual income for eligibility
    private Double totalLiabilities; // Total outstanding liabilities
    private String createdAt;
    private String updatedAt;

 
}
