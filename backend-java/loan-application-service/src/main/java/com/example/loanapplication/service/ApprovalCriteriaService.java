package com.example.loanapplication.service;

import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.dto.ProfileView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service to validate loan approval criteria based on:
 * - Credit Score (minimum threshold)
 * - Income Eligibility (loan amount vs annual income)
 * - Financial Liabilities (debt-to-income ratio)
 */
@Service
public class ApprovalCriteriaService {
    private final ProfileServiceClient profileServiceClient;
    private final double minCreditScore;
    private final double incomeMultiplier;      // Annual income should be at least loan amount / multiplier
    private final double liabilityMultiplier;   // Total liabilities should not exceed loan amount * multiplier

    public ApprovalCriteriaService(
            ProfileServiceClient profileServiceClient,
            @Value("${loan.approval.min.credit.score:600}") double minCreditScore,
            @Value("${loan.approval.income.multiplier:5}") double incomeMultiplier,
            @Value("${loan.approval.liability.multiplier:0.5}") double liabilityMultiplier
    ) {
        this.profileServiceClient = profileServiceClient;
        this.minCreditScore = minCreditScore;
        this.incomeMultiplier = incomeMultiplier;
        this.liabilityMultiplier = liabilityMultiplier;
    }

    /**
     * Validates if a customer is eligible for a loan based on:
     * 1. Minimum credit score
     * 2. Income eligibility
     * 3. Financial liabilities check
     *
     * @param userId Customer ID
     * @param loanAmount Requested loan amount
     * @return ApprovalDecision with approval status and reason
     */
    public ApprovalDecision validateApprovalCriteria(String userId, double loanAmount) {
        ProfileView profile;
        try {
            profile = profileServiceClient.getProfile(userId);
        } catch (Exception e) {
            System.err.println("[APPROVAL-CRITERIA] Failed to fetch profile for userId: " + userId + ", error: " + e.getMessage());
            // Allow approval without profile validation if profile service is unavailable
            return new ApprovalDecision(true, "Approved (profile validation skipped - profile service unavailable)");
        }
        
        if (profile == null) {
            return new ApprovalDecision(true, "Approved (profile not found - validation skipped)");
        }

        // Check 1: Credit Score
        if (profile.getCreditScore() == null || profile.getCreditScore() < minCreditScore) {
            return new ApprovalDecision(false, 
                String.format("Credit score %s is below minimum required score of %.0f", 
                    profile.getCreditScore() != null ? profile.getCreditScore() : "not set", minCreditScore));
        }

        // Check 2: Income Eligibility
        if (profile.getAnnualIncome() == null || profile.getAnnualIncome() == 0) {
            return new ApprovalDecision(false, "Annual income not provided in profile");
        }
        
        double minimumRequiredIncome = loanAmount / incomeMultiplier;
        if (profile.getAnnualIncome() < minimumRequiredIncome) {
            return new ApprovalDecision(false,
                String.format("Annual income (%.0f) is insufficient. Required: %.0f for loan amount %.0f",
                    profile.getAnnualIncome(), minimumRequiredIncome, loanAmount));
        }

        // Check 3: Financial Liabilities
        if (profile.getTotalLiabilities() == null) {
            profile.setTotalLiabilities(0.0);
        }
        
        double maxAllowedLiability = loanAmount * liabilityMultiplier;
        if (profile.getTotalLiabilities() > maxAllowedLiability) {
            return new ApprovalDecision(false,
                String.format("Total liabilities (%.0f) exceed allowed limit (%.0f) for loan amount %.0f",
                    profile.getTotalLiabilities(), maxAllowedLiability, loanAmount));
        }

        return new ApprovalDecision(true, "Approved based on credit score, income, and liability checks");
    }

    /**
     * Inner class to hold approval decision details
     */
    public static class ApprovalDecision {
        private final boolean approved;
        private final String reason;

        public ApprovalDecision(boolean approved, String reason) {
            this.approved = approved;
            this.reason = reason;
        }

        public boolean isApproved() {
            return approved;
        }

        public String getReason() {
            return reason;
        }
    }
}
