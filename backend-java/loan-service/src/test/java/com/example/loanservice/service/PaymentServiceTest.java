package com.example.loanservice.service;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.Payment;
import com.example.loanservice.emi.EMISchedule;
import com.example.loanservice.emi.EMIScheduleRepository;
import com.example.loanservice.event.EMIEvent;
import com.example.loanservice.repository.PaymentRepository;
import com.example.loanservice.domain.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private LoanRepository loanRepo;

    @Mock
    private EMIScheduleRepository emiRepo;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===================== SUCCESS CASE =====================

    @Test
    @DisplayName("Should record payment successfully")
    void recordPayment_success() {
        Loan loan = new Loan();
        loan.setId("loan1");
        loan.setAmount(100000.0);
        loan.setOutstandingBalance(100000.0);
        loan.setStatus("ACTIVE");

        EMISchedule emi = new EMISchedule();
        emi.setId("emi1");
        emi.setLoanId("loan1");
        emi.setEmiAmount(5000.0);
        emi.setPrincipalAmount(4500.0);
        emi.setStatus("PENDING");

        when(loanRepo.findById("loan1")).thenReturn(Optional.of(loan));
        when(emiRepo.findById("emi1")).thenReturn(Optional.of(emi));
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.recordPayment(
                "loan1", "emi1", 5000.0, "UPI", "TXN123"
        );

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(emiRepo).save(any());
        verify(loanRepo).save(any());
    }

    // ===================== FAILURE CASES =====================

    @Test
    void recordPayment_loanNotFound() {
        when(loanRepo.findById("loan1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.recordPayment("loan1", "emi1", 5000.0, "UPI", "TXN")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void recordPayment_emiNotFound() {
        when(loanRepo.findById("loan1")).thenReturn(Optional.of(new Loan()));
        when(emiRepo.findById("emi1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.recordPayment("loan1", "emi1", 5000.0, "UPI", "TXN")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void recordPayment_emiDoesNotBelongToLoan() {
        Loan loan = new Loan();
        loan.setId("loan1");

        EMISchedule emi = new EMISchedule();
        emi.setLoanId("loan2");

        when(loanRepo.findById("loan1")).thenReturn(Optional.of(loan));
        when(emiRepo.findById("emi1")).thenReturn(Optional.of(emi));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.recordPayment("loan1", "emi1", 5000.0, "UPI", "TXN")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void recordPayment_emiAlreadyPaid() {
        Loan loan = new Loan();
        loan.setId("loan1");

        EMISchedule emi = new EMISchedule();
        emi.setLoanId("loan1");
        emi.setStatus("PAID");

        when(loanRepo.findById("loan1")).thenReturn(Optional.of(loan));
        when(emiRepo.findById("emi1")).thenReturn(Optional.of(emi));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.recordPayment("loan1", "emi1", 5000.0, "UPI", "TXN")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void recordPayment_amountMismatch() {
        Loan loan = new Loan();
        loan.setId("loan1");

        EMISchedule emi = new EMISchedule();
        emi.setLoanId("loan1");
        emi.setEmiAmount(5000.0);
        emi.setStatus("PENDING");

        when(loanRepo.findById("loan1")).thenReturn(Optional.of(loan));
        when(emiRepo.findById("emi1")).thenReturn(Optional.of(emi));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.recordPayment("loan1", "emi1", 4000.0, "UPI", "TXN")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ===================== LOAN CLOSURE =====================

    @Test
    void recordPayment_shouldCloseLoan_whenBalanceZero() {
        Loan loan = new Loan();
        loan.setId("loan1");
        loan.setUserId("user1");
        loan.setOutstandingBalance(4500.0);

        EMISchedule emi = new EMISchedule();
        emi.setId("emi1");
        emi.setLoanId("loan1");
        emi.setEmiAmount(5000.0);
        emi.setPrincipalAmount(4500.0);
        emi.setStatus("PENDING");

        when(loanRepo.findById("loan1")).thenReturn(Optional.of(loan));
        when(emiRepo.findById("emi1")).thenReturn(Optional.of(emi));
        when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentService.recordPayment("loan1", "emi1", 5000.0, "CARD", "TXN999");

        verify(notificationPublisher).publishLoanClosed(any(EMIEvent.class));
        assertEquals("CLOSED", loan.getStatus());
    }

    // ===================== READ METHODS =====================

    @Test
    void getPaymentsByLoan_success() {
        when(paymentRepo.findByLoanIdOrderByCreatedAtDesc("loan1"))
                .thenReturn(List.of(new Payment()));

        assertEquals(1, paymentService.getPaymentsByLoan("loan1").size());
    }

    @Test
    void getPaymentsByEmi_success() {
        when(paymentRepo.findByEmiId("emi1"))
                .thenReturn(List.of(new Payment()));

        assertEquals(1, paymentService.getPaymentsByEmi("emi1").size());
    }

    @Test
    void getPayment_notFound() {
        when(paymentRepo.findById("p1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.getPayment("p1")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
