package com.example.loanapplication.service;

import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.dto.ProfileView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApprovalCriteriaServiceTest {

    @Mock
    private ProfileServiceClient profileServiceClient;

    private ApprovalCriteriaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service = new ApprovalCriteriaService(
                profileServiceClient,
                650.0,
                30000.0,
                0.4
        );
    }

    @Test
    void validateApprovalCriteria_withValidProfile_executes() {
        ProfileView profile = new ProfileView();
        profile.setCreditScore(750.0);
        profile.setAnnualIncome(600000.0);
        profile.setTotalLiabilities(100000.0);

        when(profileServiceClient.getProfile("user1")).thenReturn(profile);

        ApprovalCriteriaService.ApprovalDecision decision =
                service.validateApprovalCriteria("user1", 100000.0);

        assertNotNull(decision);
        assertNotNull(decision.getReason());
    }

    @Test
    void validateApprovalCriteria_lowCreditScore_executes() {
        ProfileView profile = new ProfileView();
        profile.setCreditScore(500.0);
        profile.setAnnualIncome(600000.0);
        profile.setTotalLiabilities(100000.0);

        when(profileServiceClient.getProfile("user1")).thenReturn(profile);

        ApprovalCriteriaService.ApprovalDecision decision =
                service.validateApprovalCriteria("user1", 100000.0);

        assertNotNull(decision);
    }

    @Test
    void validateApprovalCriteria_lowIncome_executes() {
        ProfileView profile = new ProfileView();
        profile.setCreditScore(750.0);
        profile.setAnnualIncome(10000.0);
        profile.setTotalLiabilities(1000.0);

        when(profileServiceClient.getProfile("user1")).thenReturn(profile);

        ApprovalCriteriaService.ApprovalDecision decision =
                service.validateApprovalCriteria("user1", 50000.0);

        assertNotNull(decision);
    }

    @Test
    void validateApprovalCriteria_highDebt_executes() {
        ProfileView profile = new ProfileView();
        profile.setCreditScore(750.0);
        profile.setAnnualIncome(500000.0);
        profile.setTotalLiabilities(450000.0);

        when(profileServiceClient.getProfile("user1")).thenReturn(profile);

        ApprovalCriteriaService.ApprovalDecision decision =
                service.validateApprovalCriteria("user1", 200000.0);

        assertNotNull(decision);
    }

    @Test
    void validateApprovalCriteria_nullProfile_executes() {
        when(profileServiceClient.getProfile("user1")).thenReturn(null);

        ApprovalCriteriaService.ApprovalDecision decision =
                service.validateApprovalCriteria("user1", 50000.0);

        assertNotNull(decision);
    }
}
