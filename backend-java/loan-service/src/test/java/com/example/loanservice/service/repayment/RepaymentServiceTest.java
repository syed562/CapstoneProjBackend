package com.example.loanservice.repayment;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import com.example.loanservice.emi.EMISchedule;
import com.example.loanservice.emi.EMIService;
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

@DisplayName("RepaymentService Tests")
class RepaymentServiceTest {

    @Mock
    private RepaymentRepository repaymentRepository;

    @Mock
    private EMIService emiService;

    @Mock
    private LoanRepository loanRepository;

    private RepaymentService repaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repaymentService = new RepaymentService(
                repaymentRepository,
                emiService,
                loanRepository
        );
    }

    @Test
    @DisplayName("Should record repayment successfully")
    void recordPayment_success() {
        String loanId = "loan1";
        String emiId = "emi1";

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(new Loan()));

        when(emiService.getEMIScheduleItem(emiId))
                .thenReturn(new EMISchedule());

        when(repaymentRepository.save(any(Repayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Repayment repayment = repaymentService.recordPayment(
                loanId,
                emiId,
                5000.0,
                "ONLINE",
                "TXN123"
        );

        assertNotNull(repayment);
        assertEquals(loanId, repayment.getLoanId());
        assertEquals("COMPLETED", repayment.getStatus());

        verify(emiService).markAsPaid(emiId);
        verify(repaymentRepository).save(any());
    }

    @Test
    @DisplayName("Should throw 404 when loan not found")
    void recordPayment_loanNotFound() {
        when(loanRepository.findById("loanX"))
                .thenReturn(Optional.empty());

        when(emiService.getEMIScheduleItem("emi1"))
                .thenReturn(new EMISchedule());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> repaymentService.recordPayment(
                        "loanX", "emi1", 5000.0, "ONLINE", "TXN"
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Should get repayments by loan")
    void getRepaymentsByLoan() {
        when(repaymentRepository.findByLoanId("loan1"))
                .thenReturn(List.of(new Repayment(), new Repayment()));

        List<Repayment> repayments =
                repaymentService.getRepaymentsByLoan("loan1");

        assertEquals(2, repayments.size());
    }

    @Test
    @DisplayName("Should get repayments by EMI")
    void getRepaymentsByEMI() {
        when(repaymentRepository.findByEmiScheduleId("emi1"))
                .thenReturn(List.of(new Repayment()));

        List<Repayment> repayments =
                repaymentService.getRepaymentsByEMI("emi1");

        assertEquals(1, repayments.size());
    }

    @Test
    @DisplayName("Should calculate outstanding balance")
    void getOutstandingBalance_success() {
        Loan loan = new Loan();
        loan.setId("loan1");

        EMISchedule s1 = new EMISchedule();
        s1.setStatus("SCHEDULED");
        s1.setOutstandingBalance(8000.0);

        EMISchedule s2 = new EMISchedule();
        s2.setStatus("PAID");
        s2.setOutstandingBalance(0.0);

        when(loanRepository.findById("loan1"))
                .thenReturn(Optional.of(loan));

        when(emiService.getEMISchedule("loan1"))
                .thenReturn(List.of(s1, s2));

        double balance =
                repaymentService.getOutstandingBalance("loan1");

        assertEquals(8000.0, balance);
    }

    @Test
    @DisplayName("Should throw when loan not found for outstanding balance")
    void getOutstandingBalance_loanNotFound() {
        when(loanRepository.findById("loanX"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> repaymentService.getOutstandingBalance("loanX")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Should count completed payments")
    void getCompletedPayments() {
        Repayment r1 = new Repayment();
        r1.setStatus("COMPLETED");

        Repayment r2 = new Repayment();
        r2.setStatus("FAILED");

        when(repaymentRepository.findByLoanId("loan1"))
                .thenReturn(List.of(r1, r2));

        long count =
                repaymentService.getCompletedPayments("loan1");

        assertEquals(1, count);
    }
}
