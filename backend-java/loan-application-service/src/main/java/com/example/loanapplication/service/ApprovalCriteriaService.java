package com.example.loanapplication.service;

import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.dto.ProfileView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to validate loan approval criteria based on:
 * - Credit Score (minimum threshold)
 * - Income Eligibility (loan amount vs annual income)
 * - Financial Liabilities (debt-to-income ratio)
 */
@Service
public class ApprovalCriteriaService {
    private static final Logger logger = LoggerFactory.getLogger(ApprovalCriteriaService.class);
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

   
    public ApprovalDecision validateApprovalCriteria(String userId, double loanAmount) {
        ProfileView profile;
        try {
            profile = profileServiceClient.getProfile(userId);
        } catch (Exception e) {
            logger.error("[APPROVAL-CRITERIA] Profile service unavailable for userId: {}. Skipping validation and approving. ERROR: {}", userId, e.getMessage());
            // Allow approval without profile validation if profile service is unavailable
            return new ApprovalDecision(true, "Approved (profile validation skipped - profile service unavailable)");
        }
        if (profile == null) {
            logger.error("[APPROVAL-CRITERIA] Profile not found for userId: {}. Skipping validation and approving.", userId);
            return new ApprovalDecision(true, "Approved (profile not found - validation skipped)");
        }

        logger.info("[APPROVAL-CRITERIA] Checking credit score: {} (min required: {})", profile.getCreditScore(), minCreditScore);
        if (profile.getCreditScore() == null || profile.getCreditScore() < minCreditScore) {
            logger.warn("[APPROVAL-CRITERIA] Credit score check failed: {} < {}", profile.getCreditScore(), minCreditScore);
            return new ApprovalDecision(false, 
                String.format("Credit score %s is below minimum required score of %.0f", 
                    profile.getCreditScore() != null ? profile.getCreditScore() : "not set", minCreditScore));
        }

        logger.info("[APPROVAL-CRITERIA] Checking annual income: {}", profile.getAnnualIncome());
        if (profile.getAnnualIncome() == null || profile.getAnnualIncome() == 0) {
            logger.warn("[APPROVAL-CRITERIA] Annual income not provided in profile");
            return new ApprovalDecision(false, "Annual income not provided in profile");
        }
        
        double minimumRequiredIncome = loanAmount / incomeMultiplier;
        logger.info("[APPROVAL-CRITERIA] Checking income: {} >= required {} (loan amount: {}, multiplier: {})", profile.getAnnualIncome(), minimumRequiredIncome, loanAmount, incomeMultiplier);
        if (profile.getAnnualIncome() < minimumRequiredIncome) {
            logger.warn("[APPROVAL-CRITERIA] Income check failed: {} < required {}", profile.getAnnualIncome(), minimumRequiredIncome);
            return new ApprovalDecision(false,
                String.format("Annual income (%.0f) is insufficient. Required: %.0f for loan amount %.0f",
                    profile.getAnnualIncome(), minimumRequiredIncome, loanAmount));
        }

        // Check 3: Financial Liabilities
        if (profile.getTotalLiabilities() == null) {
            profile.setTotalLiabilities(0.0);
        }
        double maxAllowedLiability = loanAmount * liabilityMultiplier;
        logger.info("[APPROVAL-CRITERIA] Checking liabilities: {} <= allowed {} (loan amount: {}, multiplier: {})", profile.getTotalLiabilities(), maxAllowedLiability, loanAmount, liabilityMultiplier);
        if (profile.getTotalLiabilities() > maxAllowedLiability) {
            logger.warn("[APPROVAL-CRITERIA] Liability check failed: {} > allowed {}", profile.getTotalLiabilities(), maxAllowedLiability);
            return new ApprovalDecision(false,
                String.format("Total liabilities (%.0f) exceed allowed limit (%.0f) for loan amount %.0f",
                    profile.getTotalLiabilities(), maxAllowedLiability, loanAmount));
        }

        logger.info("[APPROVAL-CRITERIA] All checks passed. Approving loan.");
        return new ApprovalDecision(true, "Approved based on credit score, income, and liability checks");
    }

   //Nested class to represent approval decision
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
