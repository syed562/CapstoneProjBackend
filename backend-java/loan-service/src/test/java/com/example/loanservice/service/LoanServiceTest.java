package com.example.loanservice.service;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.emi.EMI;
import com.example.loanservice.repository.LoanRepository;
import com.example.loanservice.repository.EMIRepository;
import com.example.loanservice.client.LoanApplicationClient;
import com.example.loanservice.client.dto.LoanApplicationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LoanService Tests")
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepo;

    @Mock
    private EMIRepository emiRepo;

    @Mock
    private LoanApplicationClient loanApplicationClient;

    @Mock
    private EMICalculationService emiCalculationService;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanService(loanRepo, emiRepo, loanApplicationClient, emiCalculationService);
    }

    @Test
    @DisplayName("Should create loan successfully")
    void testCreateLoanSuccess() {
        // Arrange
        String userId = "user123";
        String loanType = "PERSONAL";
        Double amount = 100000.0;
        Integer termMonths = 24;
        Double ratePercent = 12.0;

        Loan savedLoan = new Loan();
        savedLoan.setId(UUID.randomUUID().toString());
        savedLoan.setUserId(userId);
        savedLoan.setLoanType(loanType);
        savedLoan.setAmount(amount);
        savedLoan.setTermMonths(termMonths);
        savedLoan.setRatePercent(ratePercent);
        savedLoan.setStatus("ACTIVE");

        when(loanRepo.save(any(Loan.class))).thenReturn(savedLoan);

        // Act
        Loan result = loanService.create(userId, loanType, amount, termMonths, ratePercent);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(loanType, result.getLoanType());
        assertEquals(amount, result.getAmount());
        verify(loanRepo, times(1)).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should list all loans")
    void testListAllLoans() {
        // Arrange
        Loan loan1 = new Loan();
        loan1.setId("loan1");
        Loan loan2 = new Loan();
        loan2.setId("loan2");

        when(loanRepo.findAll()).thenReturn(List.of(loan1, loan2));

        // Act
        List<Loan> result = loanService.list();

        // Assert
        assertEquals(2, result.size());
        verify(loanRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Should list loans with pagination")
    void testListLoansWithPagination() {
        // Arrange
        Loan loan1 = new Loan();
        loan1.setId("loan1");
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> page = new PageImpl<>(List.of(loan1), pageable, 1);

        when(loanRepo.findAll(pageable)).thenReturn(page);

        // Act
        Page<Loan> result = loanService.listPaged(0, 10, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(loanRepo, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get loan by ID")
    void testGetLoanSuccess() {
        // Arrange
        String loanId = "loan123";
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setStatus("ACTIVE");

        when(loanRepo.findById(loanId)).thenReturn(Optional.of(loan));

        // Act
        Loan result = loanService.get(loanId);

        // Assert
        assertNotNull(result);
        assertEquals(loanId, result.getId());
        verify(loanRepo, times(1)).findById(loanId);
    }

    @Test
    @DisplayName("Should throw 404 when loan not found")
    void testGetLoanNotFound() {
        // Arrange
        String loanId = "nonexistent";
        when(loanRepo.findById(loanId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                loanService.get(loanId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should update loan status")
    void testUpdateStatusSuccess() {
        // Arrange
        String loanId = "loan123";
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setStatus("ACTIVE");

        when(loanRepo.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any(Loan.class))).thenReturn(loan);

        // Act
        Loan result = loanService.updateStatus(loanId, "CLOSED");

        // Assert
        assertNotNull(result);
        verify(loanRepo, times(1)).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should list loans by user")
    void testListByUser() {
        // Arrange
        String userId = "user123";
        Loan loan = new Loan();
        loan.setUserId(userId);

        when(loanRepo.findByUserId(userId)).thenReturn(List.of(loan));

        // Act
        List<Loan> result = loanService.listByUser(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(loanRepo, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should find loans by status and amount range")
    void testFindByStatusAndAmount() {
        // Arrange
        Loan loan = new Loan();
        loan.setStatus("ACTIVE");
        loan.setAmount(100000.0);

        when(loanRepo.findByStatusAndAmountBetween("ACTIVE", 50000.0, 150000.0))
                .thenReturn(List.of(loan));

        // Act
        List<Loan> result = loanService.findByStatusAndAmount("ACTIVE", 50000.0, 150000.0);

        // Assert
        assertEquals(1, result.size());
        verify(loanRepo, times(1)).findByStatusAndAmountBetween(anyString(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should generate EMI schedule for loan")
    void testGenerateEMISuccess() {
        // Arrange
        String loanId = "loan123";
        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setAmount(100000.0);
        loan.setTermMonths(24);
        loan.setRatePercent(12.0);

        when(loanRepo.findById(loanId)).thenReturn(Optional.of(loan));
        when(emiCalculationService.calculateEMI(100000.0, 24, 12.0)).thenReturn(4400.0);
        when(emiRepo.save(any(EMI.class))).thenReturn(new EMI());

        // Act
        loanService.generateEMIForLoan(loanId);

        // Assert
        verify(emiCalculationService, times(1)).calculateEMI(anyDouble(), anyInt(), anyDouble());
        verify(emiRepo, atLeastOnce()).save(any(EMI.class));
    }

    @Test
    @DisplayName("Should delete loan")
    void testDeleteLoan() {
        // Arrange
        String loanId = "loan123";
        Loan loan = new Loan();
        loan.setId(loanId);

        when(loanRepo.findById(loanId)).thenReturn(Optional.of(loan));
        doNothing().when(loanRepo).delete(any(Loan.class));

        // Act
        loanService.delete(loanId);

        // Assert
        verify(loanRepo, times(1)).delete(any(Loan.class));
    }
}
