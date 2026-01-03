package com.example.loanservice.emi;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EMIServiceTest {

    @Mock
    private EMIScheduleRepository emiRepository;

    @Mock
    private LoanRepository loanRepository;

    private EMIService emiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emiService = new EMIService(emiRepository, loanRepository);
    }

    @Test
    void shouldThrow404WhenLoanNotFound() {
        when(loanRepository.findById("loan1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                emiService.generateEMISchedule("loan1")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldThrow400WhenInterestRateInvalid() {
        Loan loan = new Loan();
        loan.setId("loan1");
        loan.setAmount(100000.0);
        loan.setRatePercent(0.0);
        loan.setTermMonths(12);

        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                emiService.generateEMISchedule("loan1")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldGenerateEmiScheduleSuccessfully() {
        Loan loan = new Loan();
        loan.setId("loan1");
        loan.setAmount(120000.0);
        loan.setRatePercent(12.0);
        loan.setTermMonths(12);

        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(emiRepository.findByLoanId("loan1")).thenReturn(List.of());
        when(emiRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<EMISchedule> schedules = emiService.generateEMISchedule("loan1");

        assertEquals(12, schedules.size());
        verify(emiRepository).saveAll(any());
    }

    @Test
    void shouldDeleteExistingScheduleBeforeGeneratingNew() {
        Loan loan = new Loan();
        loan.setId("loan1");
        loan.setAmount(100000.0);
        loan.setRatePercent(10.0);
        loan.setTermMonths(6);

        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(emiRepository.findByLoanId("loan1")).thenReturn(List.of(new EMISchedule()));
        when(emiRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        emiService.generateEMISchedule("loan1");

        verify(emiRepository).deleteAll(any());
    }

    @Test
    void shouldGetEmiScheduleByLoanId() {
        when(emiRepository.findByLoanIdOrderByMonth("loan1"))
                .thenReturn(List.of(new EMISchedule()));

        List<EMISchedule> result = emiService.getEMISchedule("loan1");

        assertEquals(1, result.size());
    }

    @Test
    void shouldThrow404WhenScheduleNotFound() {
        when(emiRepository.findById("emi1")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                emiService.getEMIScheduleItem("emi1")
        );
    }

    @Test
    void shouldMarkEmiAsPaid() {
        EMISchedule schedule = new EMISchedule();
        schedule.setId("emi1");
        schedule.setStatus("SCHEDULED");

        when(emiRepository.findById("emi1")).thenReturn(Optional.of(schedule));
        when(emiRepository.save(any())).thenReturn(schedule);

        EMISchedule result = emiService.markAsPaid("emi1");

        assertEquals("PAID", result.getStatus());
        verify(emiRepository).save(any());
    }
}
