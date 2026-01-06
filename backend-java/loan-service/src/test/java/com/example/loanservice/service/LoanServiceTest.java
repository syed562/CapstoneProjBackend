package com.example.loanservice.service;

import com.example.loanservice.client.LoanApplicationClient;
import com.example.loanservice.client.dto.LoanApplicationView;
import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import com.example.loanservice.domain.LoanType;
import com.example.loanservice.emi.EMIService;
import com.example.loanservice.controller.dto.CreateLoanRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock
    private LoanRepository repo;

    @Mock
    private LoanApplicationClient loanApplicationClient;

    @Mock
    private EMIService emiService;

    @Mock
    private LoanNotificationService notificationService;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanService(
                repo,
                loanApplicationClient,
                emiService,
                notificationService,
                "PERSONAL=12,HOME=8.5,AUTO=10"
        );
    }

    @Test
    void list_shouldReturnAllLoans() {
        when(repo.findAll()).thenReturn(List.of(new Loan()));
        assertEquals(1, loanService.list().size());
    }

    @Test
    void listByUser_blankUser_shouldThrow400() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> loanService.listByUser(" "));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void get_notFound_shouldThrow404() {
        when(repo.findById("id")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> loanService.get("id"));
    }

    @Test
    void create_shouldUseDefaultRate() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        Loan loan = loanService.create("u1", LoanType.PERSONAL, 10000, 12, null);
        assertEquals(12.0, loan.getRatePercent());
    }

    @Test
    void updateStatus_success() {
        Loan loan = new Loan();
        when(repo.findById("id")).thenReturn(Optional.of(loan));
        when(repo.save(any())).thenReturn(loan);

        Loan updated = loanService.updateStatus("id", "CLOSED");
        assertEquals("CLOSED", updated.getStatus());
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(repo.existsById("x")).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> loanService.delete("x"));
    }

    @Test
    void generateEMIForLoan_notFound_shouldThrow() {
        when(repo.existsById("x")).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> loanService.generateEMIForLoan("x"));
    }

    @Test
    void approveFromApplication_success() {
        LoanApplicationView view = new LoanApplicationView();
        view.setUserId("u1");
        view.setAmount(50000.0);
        view.setLoanType("PERSONAL");
        view.setTermMonths(12);
        view.setRatePercent(10.0);

        when(loanApplicationClient.getApplication("app1")).thenReturn(view);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Loan loan = loanService.approveFromApplication("app1");
        assertEquals("approved", loan.getStatus());
        verify(emiService).generateEMISchedule(any());
        verify(notificationService).sendEMINotification(any(), any(), anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void createLoanFromApplication_shouldCreateLoan() {
        CreateLoanRequest req = new CreateLoanRequest();
        req.setUserId("u1");
        req.setAmount(10000.0);
        req.setLoanType("AUTO");
        req.setTermMonths(24);

        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> loanService.createLoanFromApplication(req));
        verify(emiService).generateEMISchedule(any());
    }
}
