package com.example.loanapplication.service;

import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.MODELS.LoanType;
import com.example.loanapplication.client.LoanServiceClient;
import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.dto.ProfileView;
import com.example.loanapplication.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LoanApplicationService Tests")
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repo;

    @Mock
    private ApprovalCriteriaService approvalCriteriaService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProfileServiceClient profileServiceClient;

    @Mock
    private LoanServiceClient loanServiceClient;

    @Mock
    private NotificationPublisher notificationPublisher;

    private LoanApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new LoanApplicationService(
                repo,
                approvalCriteriaService,
                notificationService,
                profileServiceClient,
                loanServiceClient,
                notificationPublisher,
                5000.0,  // minAmount
                2000000.0,  // maxAmount
                "12,24,36",  // tenures
                "PERSONAL=12,HOME=8.5,AUTO=10,EDUCATIONAL=7.5,HOME_LOAN=8.5"  // rates
        );
    }

    @Test
    @DisplayName("Should successfully apply for loan with valid data")
    void testApplySuccess() {
        // Arrange
        String userId = "user123";
        LoanType loanType = LoanType.PERSONAL;
        double amount = 100000;
        int termMonths = 24;

        when(repo.findByUserIdAndLoanTypeAndStatusIn(userId, loanType, 
                List.of("SUBMITTED", "UNDER_REVIEW", "APPROVED"))).thenReturn(List.of());
        
        LoanApplication savedApp = new LoanApplication();
        savedApp.setId(UUID.randomUUID().toString());
        savedApp.setUserId(userId);
        savedApp.setLoanType(loanType);
        savedApp.setAmount(String.valueOf(amount));
        savedApp.setTermMonths(termMonths);
        savedApp.setRatePercent("12");
        savedApp.setStatus("SUBMITTED");

        when(repo.save(any(LoanApplication.class))).thenReturn(savedApp);
        doNothing().when(notificationPublisher).publishApplicationCreated(any());

        // Act
        LoanApplication result = service.apply(userId, loanType, amount, termMonths, null);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(loanType, result.getLoanType());
        assertEquals("SUBMITTED", result.getStatus());
        verify(repo, times(1)).save(any());
    }

    @Test
    @DisplayName("Should reject application with amount below minimum")
    void testApplyBelowMinAmount() {
        // Arrange
        String userId = "user123";
        double amount = 1000;  // Below minimum of 5000

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.apply(userId, LoanType.PERSONAL, amount, 24, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Amount must be between"));
    }

    @Test
    @DisplayName("Should reject application with invalid tenure")
    void testApplyInvalidTenure() {
        // Arrange
        String userId = "user123";

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.apply(userId, LoanType.PERSONAL, 100000, 18, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Term must be one of"));
    }

    @Test
    @DisplayName("Should reject if user has active application for same loan type")
    void testApplyDuplicateActiveLoan() {
        // Arrange
        String userId = "user123";
        LoanType loanType = LoanType.PERSONAL;

        LoanApplication existingApp = new LoanApplication();
        existingApp.setId("existing123");
        existingApp.setStatus("UNDER_REVIEW");

        when(repo.findByUserIdAndLoanTypeAndStatusIn(userId, loanType,
                List.of("SUBMITTED", "UNDER_REVIEW", "APPROVED")))
                .thenReturn(List.of(existingApp));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.apply(userId, loanType, 100000, 24, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("active application"));
    }

    @Test
    @DisplayName("Should use custom rate if provided")
    void testApplyWithCustomRate() {
        // Arrange
        String userId = "user123";
        LoanType loanType = LoanType.PERSONAL;
        double customRate = 15.0;

        when(repo.findByUserIdAndLoanTypeAndStatusIn(userId, loanType,
                List.of("SUBMITTED", "UNDER_REVIEW", "APPROVED"))).thenReturn(List.of());

        LoanApplication savedApp = new LoanApplication();
        savedApp.setId("app123");
        savedApp.setRatePercent(String.valueOf(customRate));

        when(repo.save(any(LoanApplication.class))).thenReturn(savedApp);
        doNothing().when(notificationPublisher).publishApplicationCreated(any());

        // Act
        LoanApplication result = service.apply(userId, loanType, 100000, 24, customRate);

        // Assert
        assertEquals(customRate, Double.parseDouble(result.getRatePercent()));
    }

    @Test
    @DisplayName("Should retrieve loan application by ID")
    void testGetApplicationSuccess() {
        // Arrange
        String appId = "app123";
        LoanApplication app = new LoanApplication();
        app.setId(appId);
        app.setStatus("SUBMITTED");

        when(repo.findById(appId)).thenReturn(Optional.of(app));

        // Act
        LoanApplication result = service.get(appId);

        // Assert
        assertNotNull(result);
        assertEquals(appId, result.getId());
        verify(repo, times(1)).findById(appId);
    }

    @Test
    @DisplayName("Should throw 404 when application not found")
    void testGetApplicationNotFound() {
        // Arrange
        String appId = "nonexistent";
        when(repo.findById(appId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.get(appId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should mark application as UNDER_REVIEW")
    void testMarkUnderReviewSuccess() {
        // Arrange
        String appId = "app123";
        LoanApplication app = new LoanApplication();
        app.setId(appId);
        app.setStatus("SUBMITTED");

        when(repo.findById(appId)).thenReturn(Optional.of(app));
        when(repo.save(any(LoanApplication.class))).thenReturn(app);

        // Act
        LoanApplication result = service.markUnderReview(appId);

        // Assert
        assertEquals("UNDER_REVIEW", result.getStatus());
        verify(repo, times(1)).save(any());
    }

    @Test
    @DisplayName("Should approve application successfully")
    void testApproveSuccess() {
        // Arrange
        String appId = "app123";
        LoanApplication app = new LoanApplication();
        app.setId(appId);
        app.setUserId("user123");
        app.setStatus("UNDER_REVIEW");
        app.setAmount("100000");
        app.setTermMonths(24);
        app.setRatePercent("12");
        app.setLoanType(LoanType.PERSONAL);

        ApprovalCriteriaService.ApprovalDecision decision = new ApprovalCriteriaService.ApprovalDecision(
                true, "Approved"
        );

        when(repo.findById(appId)).thenReturn(Optional.of(app));
        when(approvalCriteriaService.validateApprovalCriteria("user123", 100000)).thenReturn(decision);
        when(repo.save(any(LoanApplication.class))).thenReturn(app);
        doNothing().when(notificationService).sendApprovalNotification(anyString(), anyString(), 
                anyDouble(), anyInt(), anyDouble(), anyString());
        doNothing().when(loanServiceClient).createLoanFromApplication(any());
        doNothing().when(notificationPublisher).publishApplicationApproved(any());

        // Act
        LoanApplication result = service.approve(appId);

        // Assert
        assertEquals("APPROVED", result.getStatus());
        verify(repo, times(1)).save(any());
        verify(loanServiceClient, times(1)).createLoanFromApplication(any());
    }

    @Test
    @DisplayName("Should reject application with remarks")
    void testRejectSuccess() {
        // Arrange
        String appId = "app123";
        String remarks = "Insufficient income";
        LoanApplication app = new LoanApplication();
        app.setId(appId);
        app.setUserId("user123");
        app.setStatus("UNDER_REVIEW");
        app.setAmount("100000");

        when(repo.findById(appId)).thenReturn(Optional.of(app));
        when(repo.save(any(LoanApplication.class))).thenReturn(app);
        doNothing().when(notificationPublisher).publishApplicationRejected(any());

        // Act
        LoanApplication result = service.reject(appId, remarks);

        // Assert
        assertEquals("REJECTED", result.getStatus());
        assertEquals(remarks, result.getRemarks());
        verify(notificationPublisher, times(1)).publishApplicationRejected(any());
    }

    @Test
    @DisplayName("Should reject if rejection remarks are empty")
    void testRejectWithEmptyRemarks() {
        // Arrange
        String appId = "app123";
        LoanApplication app = new LoanApplication();
        app.setStatus("UNDER_REVIEW");

        when(repo.findById(appId)).thenReturn(Optional.of(app));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.reject(appId, "")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should list all applications by user")
    void testListByUser() {
        // Arrange
        String userId = "user123";
        LoanApplication app1 = new LoanApplication();
        app1.setUserId(userId);
        LoanApplication app2 = new LoanApplication();
        app2.setUserId(userId);

        when(repo.findByUserId(userId)).thenReturn(List.of(app1, app2));

        // Act
        List<LoanApplication> result = service.listByUser(userId);

        // Assert
        assertEquals(2, result.size());
        verify(repo, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should list all applications")
    void testListAll() {
        // Arrange
        LoanApplication app1 = new LoanApplication();
        LoanApplication app2 = new LoanApplication();

        when(repo.findAll()).thenReturn(List.of(app1, app2));

        // Act
        List<LoanApplication> result = service.listAll();

        // Assert
        assertEquals(2, result.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    @DisplayName("Should use correct default rate for EDUCATIONAL loan")
    void testEducationalLoanRate() {
        // Arrange
        String userId = "user123";
        LoanType loanType = LoanType.EDUCATIONAL;

        when(repo.findByUserIdAndLoanTypeAndStatusIn(userId, loanType,
                List.of("SUBMITTED", "UNDER_REVIEW", "APPROVED"))).thenReturn(List.of());

        LoanApplication savedApp = new LoanApplication();
        savedApp.setId("app123");
        savedApp.setRatePercent("7.5");

        when(repo.save(any(LoanApplication.class))).thenReturn(savedApp);
        doNothing().when(notificationPublisher).publishApplicationCreated(any());

        // Act
        LoanApplication result = service.apply(userId, loanType, 200000, 36, null);

        // Assert
        assertEquals("7.5", result.getRatePercent());
    }

    @Test
    @DisplayName("Should use correct default rate for HOME_LOAN")
    void testHomeLoanRate() {
        // Arrange
        String userId = "user123";
        LoanType loanType = LoanType.HOME_LOAN;

        when(repo.findByUserIdAndLoanTypeAndStatusIn(userId, loanType,
                List.of("SUBMITTED", "UNDER_REVIEW", "APPROVED"))).thenReturn(List.of());

        LoanApplication savedApp = new LoanApplication();
        savedApp.setId("app123");
        savedApp.setRatePercent("8.5");

        when(repo.save(any(LoanApplication.class))).thenReturn(savedApp);
        doNothing().when(notificationPublisher).publishApplicationCreated(any());

        // Act
        LoanApplication result = service.apply(userId, loanType, 500000, 36, null);

        // Assert
        assertEquals("8.5", result.getRatePercent());
    }
}
