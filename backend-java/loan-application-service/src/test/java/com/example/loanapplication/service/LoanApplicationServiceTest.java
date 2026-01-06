package com.example.loanapplication.service;

import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.MODELS.LoanType;
import com.example.loanapplication.client.LoanServiceClient;
import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.UserServiceClient;
import com.example.loanapplication.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LoanApplicationService – High Coverage Tests")
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
    private UserServiceClient userServiceClient;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private RateConfigService rateConfigService;

    private LoanApplicationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new LoanApplicationService(
                repo,
                approvalCriteriaService,
                notificationService,
                profileServiceClient,
                loanServiceClient,
                userServiceClient,
                notificationPublisher,
                rateConfigService,
                5000,
                2000000,
                "12,24,36",
                "PERSONAL=12,HOME=8.5,AUTO=10,EDUCATIONAL=7.5,HOME_LOAN=8.5"
        );
    }

    // --------------------------------------------------
    // APPLY
    // --------------------------------------------------

    @Test
    void apply_success() {
        when(repo.findByUserIdAndLoanTypeAndStatusIn(any(), any(), any()))
                .thenReturn(List.of());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanApplication app = service.apply(
                "user1",
                LoanType.PERSONAL,
                100000,
                24,
                null
        );

        assertEquals("SUBMITTED", app.getStatus());
        verify(repo).save(any());
        verify(notificationPublisher).publishApplicationCreated(any());
    }

    @Test
    void apply_amountBelowMin_shouldFail() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.apply("u1", LoanType.PERSONAL, 1000, 12, null)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void apply_amountAboveMax_shouldFail() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.apply("u1", LoanType.PERSONAL, 3000000, 12, null)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void apply_invalidTenure_shouldFail() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.apply("u1", LoanType.PERSONAL, 100000, 18, null)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void apply_duplicateActiveApplication_shouldFail() {
        when(repo.findByUserIdAndLoanTypeAndStatusIn(any(), any(), any()))
                .thenReturn(List.of(new LoanApplication()));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.apply("u1", LoanType.PERSONAL, 100000, 12, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void apply_customRate_shouldBeUsed() {
        when(repo.findByUserIdAndLoanTypeAndStatusIn(any(), any(), any()))
                .thenReturn(List.of());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanApplication app = service.apply(
                "u1",
                LoanType.PERSONAL,
                100000,
                24,
                15.5
        );

        assertEquals("15.5", app.getRatePercent());
    }

    @Test
    void apply_defaultRate_shouldBeResolved() {
        when(repo.findByUserIdAndLoanTypeAndStatusIn(any(), any(), any()))
                .thenReturn(List.of());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(rateConfigService.getRate("EDUCATIONAL"))
                .thenReturn(7.5);

        LoanApplication app = service.apply(
                "u1",
                LoanType.EDUCATIONAL,
                200000,
                36,
                null
        );

        assertEquals("7.5", app.getRatePercent());
    }

    // --------------------------------------------------
    // GET
    // --------------------------------------------------

    @Test
    void get_found() {
        LoanApplication app = new LoanApplication();
        when(repo.findById("1")).thenReturn(Optional.of(app));

        assertNotNull(service.get("1"));
    }

    @Test
    void get_notFound_shouldFail() {
        when(repo.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.get("1")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // --------------------------------------------------
    // MARK UNDER REVIEW
    // --------------------------------------------------

    @Test
    void markUnderReview_success() {
        LoanApplication app = new LoanApplication();
        app.setStatus("SUBMITTED");

        when(repo.findById("1")).thenReturn(Optional.of(app));
        when(repo.save(any())).thenReturn(app);

        LoanApplication result = service.markUnderReview("1");

        assertEquals("UNDER_REVIEW", result.getStatus());
    }

    @Test
    void markUnderReview_invalidStatus_shouldFail() {
        LoanApplication app = new LoanApplication();
        app.setStatus("APPROVED");

        when(repo.findById("1")).thenReturn(Optional.of(app));

        assertThrows(ResponseStatusException.class,
                () -> service.markUnderReview("1"));
    }

    // --------------------------------------------------
    // APPROVE
    // --------------------------------------------------

    @Test
    void approve_success() {
        LoanApplication app = baseUnderReviewApp();

        when(repo.findById("1")).thenReturn(Optional.of(app));
        when(repo.save(any())).thenReturn(app);
        when(approvalCriteriaService.validateApprovalCriteria(any(), anyDouble()))
                .thenReturn(new ApprovalCriteriaService.ApprovalDecision(true, "OK"));

        LoanApplication result = service.approve("1");

        assertEquals("APPROVED", result.getStatus());
        verify(notificationService).sendApprovalNotification(any(), any(), anyDouble(), anyInt(), anyDouble(), any());
        verify(loanServiceClient).createLoanFromApplication(any());
    }

    @Test
    void approve_invalidStatus_shouldFail() {
        LoanApplication app = new LoanApplication();
        app.setId("1");
        app.setUserId("u1");
        app.setStatus("REJECTED");  // Invalid status for approval
        app.setAmount("50000");
        app.setRatePercent("12");
        app.setTermMonths(12);
        app.setLoanType(LoanType.PERSONAL);
        app.setCreatedAt("2024-01-01");
        app.setUpdatedAt("2024-01-01");

        when(repo.findById("1")).thenReturn(Optional.of(app));

        assertThrows(ResponseStatusException.class,
                () -> service.approve("1"));
    }

    @Test
    void approve_rejectedByCriteria_shouldFail() {
        LoanApplication app = baseUnderReviewApp();

        when(repo.findById("1")).thenReturn(Optional.of(app));
        when(approvalCriteriaService.validateApprovalCriteria(any(), anyDouble()))
                .thenReturn(new ApprovalCriteriaService.ApprovalDecision(false, "Low credit"));

        assertThrows(ResponseStatusException.class,
                () -> service.approve("1"));
    }

    @Test
    void approve_loanServiceFailure_shouldNotFail() {
        LoanApplication app = baseUnderReviewApp();

        when(repo.findById("1")).thenReturn(Optional.of(app));
        when(repo.save(any())).thenReturn(app);
        when(approvalCriteriaService.validateApprovalCriteria(any(), anyDouble()))
                .thenReturn(new ApprovalCriteriaService.ApprovalDecision(true, "OK"));

        doThrow(new RuntimeException("Loan service down"))
                .when(loanServiceClient).createLoanFromApplication(any());

        LoanApplication result = service.approve("1");

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void approve_notificationFailure_shouldNotFail() {
        LoanApplication app = baseUnderReviewApp();

        when(repo.findById("1")).thenReturn(Optional.of(app));
        when(repo.save(any())).thenReturn(app);
        when(approvalCriteriaService.validateApprovalCriteria(any(), anyDouble()))
                .thenReturn(new ApprovalCriteriaService.ApprovalDecision(true, "OK"));

        doThrow(new RuntimeException("Notify failed"))
                .when(notificationPublisher).publishApplicationApproved(any());

        LoanApplication result = service.approve("1");

        assertEquals("APPROVED", result.getStatus());
    }

    // --------------------------------------------------
    // REJECT
    // --------------------------------------------------

    @Test
    void reject_success() {
        LoanApplication app = new LoanApplication();
        app.setStatus("UNDER_REVIEW");
        app.setAmount("50000");

        when(repo.findById("1")).thenReturn(Optional.of(app));
        when(repo.save(any())).thenReturn(app);

        LoanApplication result = service.reject("1", "Not eligible");

        assertEquals("REJECTED", result.getStatus());
    }

    @Test
    void reject_withoutRemarks_shouldFail() {
        LoanApplication app = new LoanApplication();
        app.setStatus("UNDER_REVIEW");

        when(repo.findById("1")).thenReturn(Optional.of(app));

        assertThrows(ResponseStatusException.class,
                () -> service.reject("1", ""));
    }

    // --------------------------------------------------
    // LIST
    // --------------------------------------------------

    @Test
    void listByUser() {
        when(repo.findByUserId("u1")).thenReturn(List.of());

        assertNotNull(service.listByUser("u1"));
    }

    @Test
    void listAll() {
        when(repo.findAll()).thenReturn(List.of());

        assertNotNull(service.listAll());
    }

    // --------------------------------------------------
    // HELPER
    // --------------------------------------------------

    private LoanApplication baseUnderReviewApp() {
        LoanApplication app = new LoanApplication();
        app.setId("1");
        app.setUserId("u1");
        app.setStatus("UNDER_REVIEW");
        app.setAmount("50000");
        app.setRatePercent("12");
        app.setTermMonths(12);
        app.setLoanType(LoanType.PERSONAL);
        return app;
}
}
