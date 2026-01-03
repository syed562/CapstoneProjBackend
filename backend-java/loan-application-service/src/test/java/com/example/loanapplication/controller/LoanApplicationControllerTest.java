package com.example.loanapplication.controller;

import com.example.loanapplication.DTO.ApplyRequest;
import com.example.loanapplication.DTO.ApprovalRequest;
import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.MODELS.LoanType;
import com.example.loanapplication.service.LoanApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LoanApplicationController Tests")
class LoanApplicationControllerTest {

    @Mock
    private LoanApplicationService loanApplicationService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private LoanApplicationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new LoanApplicationController(loanApplicationService);
    }

    @Test
    @DisplayName("Should apply for loan successfully")
    void testApplySuccess() {
        // Arrange
        ApplyRequest req = new ApplyRequest();
        req.setLoanType(LoanType.PERSONAL);
        req.setAmount(100000.0);
        req.setTermMonths(24);

        LoanApplication expectedApp = new LoanApplication();
        expectedApp.setId("app123");
        expectedApp.setStatus("SUBMITTED");

        when(authentication.getPrincipal()).thenReturn("user123");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(loanApplicationService.apply("user123", LoanType.PERSONAL, 100000.0, 24, null))
                .thenReturn(expectedApp);

        // Act
        ResponseEntity<LoanApplication> response = controller.apply(req);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("app123", response.getBody().getId());
        verify(loanApplicationService, times(1)).apply("user123", LoanType.PERSONAL, 100000.0, 24, null);
    }

    @Test
    @DisplayName("Should throw 401 when authentication context is missing")
    void testApplyMissingAuthContext() {
        // Arrange
        ApplyRequest req = new ApplyRequest();
        req.setLoanType(LoanType.PERSONAL);
        req.setAmount(100000.0);
        req.setTermMonths(24);

        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.apply(req)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve all applications")
    void testListAll() {
        // Arrange
        LoanApplication app1 = new LoanApplication();
        app1.setId("app1");
        LoanApplication app2 = new LoanApplication();
        app2.setId("app2");

        when(loanApplicationService.listAll()).thenReturn(List.of(app1, app2));

        // Act
        ResponseEntity<List<LoanApplication>> response = controller.listAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(loanApplicationService, times(1)).listAll();
    }

    @Test
    @DisplayName("Should retrieve applications by user")
    void testListByUser() {
        // Arrange
        String userId = "user123";
        LoanApplication app = new LoanApplication();
        app.setId("app123");
        app.setUserId(userId);

        when(loanApplicationService.listByUser(userId)).thenReturn(List.of(app));

        // Act
        ResponseEntity<List<LoanApplication>> response = controller.my(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(userId, response.getBody().get(0).getUserId());
    }

    @Test
    @DisplayName("Should retrieve single application by ID")
    void testGetApplicationById() {
        // Arrange
        String appId = "app123";
        LoanApplication app = new LoanApplication();
        app.setId(appId);

        when(loanApplicationService.get(appId)).thenReturn(app);

        // Act
        ResponseEntity<LoanApplication> response = controller.get(appId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(appId, response.getBody().getId());
        verify(loanApplicationService, times(1)).get(appId);
    }

    @Test
    @DisplayName("Should mark application as under review")
    void testMarkUnderReview() {
        // Arrange
        String appId = "app123";
        LoanApplication app = new LoanApplication();
        app.setId(appId);
        app.setStatus("UNDER_REVIEW");

        when(loanApplicationService.markUnderReview(appId)).thenReturn(app);

        // Act
        ResponseEntity<LoanApplication> response = controller.markUnderReview(appId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UNDER_REVIEW", response.getBody().getStatus());
        verify(loanApplicationService, times(1)).markUnderReview(appId);
    }

    @Test
    @DisplayName("Should approve application")
    void testApprove() {
        // Arrange
        String appId = "app123";
        LoanApplication app = new LoanApplication();
        app.setId(appId);
        app.setStatus("APPROVED");

        when(loanApplicationService.approve(appId)).thenReturn(app);

        // Act
        ResponseEntity<LoanApplication> response = controller.approve(appId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("APPROVED", response.getBody().getStatus());
        verify(loanApplicationService, times(1)).approve(appId);
    }

    @Test
    @DisplayName("Should reject application with remarks")
    void testReject() {
        // Arrange
        String appId = "app123";
        ApprovalRequest request = new ApprovalRequest();
        request.setRemarks("Insufficient income");

        LoanApplication app = new LoanApplication();
        app.setId(appId);
        app.setStatus("REJECTED");
        app.setRemarks("Insufficient income");

        when(loanApplicationService.reject(appId, "Insufficient income")).thenReturn(app);

        // Act
        ResponseEntity<LoanApplication> response = controller.reject(appId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("REJECTED", response.getBody().getStatus());
        assertEquals("Insufficient income", response.getBody().getRemarks());
        verify(loanApplicationService, times(1)).reject(appId, "Insufficient income");
    }
}
