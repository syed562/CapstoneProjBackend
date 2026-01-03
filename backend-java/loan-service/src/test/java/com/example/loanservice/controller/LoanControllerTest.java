package com.example.loanservice.controller;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.Payment;
import com.example.loanservice.service.LoanService;
import com.example.loanservice.service.PaymentService;
import com.example.loanservice.controller.dto.CreateLoanRequest;
import com.example.loanservice.controller.dto.UpdateStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LoanController Tests")
class LoanControllerTest {

    @Mock
    private LoanService loanService;

    private LoanController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new LoanController(loanService);
    }

    @Test
    @DisplayName("Should list all loans")
    void testListLoans() {
        // Arrange
        Loan loan1 = new Loan();
        loan1.setId("loan1");
        Loan loan2 = new Loan();
        loan2.setId("loan2");

        when(loanService.list()).thenReturn(List.of(loan1, loan2));

        // Act
        List<Loan> result = controller.list();

        // Assert
        assertEquals(2, result.size());
        verify(loanService, times(1)).list();
    }

    @Test
    @DisplayName("Should list loans with pagination")
    void testListPaged() {
        // Arrange
        Loan loan = new Loan();
        loan.setId("loan1");

        Page<Loan> page = new PageImpl<>(List.of(loan), PageRequest.of(0, 20), 1);
        when(loanService.listPaged(0, 20, any())).thenReturn(page);

        // Act
        Page<Loan> result = controller.listPaged(0, 20, "createdAt,desc");

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(loanService, times(1)).listPaged(anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Should list loans by user")
    void testMyLoans() {
        // Arrange
        String userId = "user123";
        Loan loan = new Loan();
        loan.setUserId(userId);

        when(loanService.listByUser(userId)).thenReturn(List.of(loan));

        // Act
        List<Loan> result = controller.myLoans(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }

    @Test
    @DisplayName("Should filter loans by status and amount")
    void testFilterLoans() {
        // Arrange
        Loan loan = new Loan();
        loan.setStatus("ACTIVE");
        loan.setAmount(100000.0);

        when(loanService.findByStatusAndAmount("ACTIVE", 50000.0, 150000.0))
                .thenReturn(List.of(loan));

        // Act
        List<Loan> result = controller.filterLoans("ACTIVE", 50000.0, 150000.0);

        // Assert
        assertEquals(1, result.size());
        verify(loanService, times(1)).findByStatusAndAmount("ACTIVE", 50000.0, 150000.0);
    }

    @Test
    @DisplayName("Should get loan by ID")
    void testGetLoan() {
        // Arrange
        String loanId = "loan123";
        Loan loan = new Loan();
        loan.setId(loanId);

        when(loanService.get(loanId)).thenReturn(loan);

        // Act
        Loan result = controller.get(loanId);

        // Assert
        assertEquals(loanId, result.getId());
        verify(loanService, times(1)).get(loanId);
    }

    @Test
    @DisplayName("Should create loan")
    void testCreateLoan() {
        // Arrange
        CreateLoanRequest req = new CreateLoanRequest();
        req.setUserId("user123");
        req.setLoanType("PERSONAL");
        req.setAmount(100000.0);
        req.setTermMonths(24);
        req.setRatePercent(12.0);

        Loan loan = new Loan();
        loan.setId("loan123");

        when(loanService.create("user123", "PERSONAL", 100000.0, 24, 12.0)).thenReturn(loan);

        // Act
        Loan result = controller.create(req);

        // Assert
        assertNotNull(result);
        assertEquals("loan123", result.getId());
        verify(loanService, times(1)).create(anyString(), anyString(), anyDouble(), anyInt(), anyDouble());
    }

    @Test
    @DisplayName("Should update loan status")
    void testUpdateStatus() {
        // Arrange
        String loanId = "loan123";
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus("CLOSED");

        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setStatus("CLOSED");

        when(loanService.updateStatus(loanId, "CLOSED")).thenReturn(loan);

        // Act
        Loan result = controller.updateStatus(loanId, req);

        // Assert
        assertEquals("CLOSED", result.getStatus());
        verify(loanService, times(1)).updateStatus(loanId, "CLOSED");
    }

    @Test
    @DisplayName("Should generate EMI schedule")
    void testGenerateEMI() {
        // Arrange
        String loanId = "loan123";
        doNothing().when(loanService).generateEMIForLoan(loanId);

        // Act
        ResponseEntity<String> result = controller.generateEMI(loanId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("EMI schedule generated"));
        verify(loanService, times(1)).generateEMIForLoan(loanId);
    }

    @Test
    @DisplayName("Should delete loan")
    void testDeleteLoan() {
        // Arrange
        String loanId = "loan123";
        doNothing().when(loanService).delete(loanId);

        // Act
        ResponseEntity<Void> result = controller.delete(loanId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(loanService, times(1)).delete(loanId);
    }
}

@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    private PaymentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PaymentController(paymentService);
    }

    @Test
    @DisplayName("Should record payment successfully")
    void testRecordPaymentSuccess() {
        // Arrange
        RecordPaymentRequest req = new RecordPaymentRequest();
        req.setLoanId("loan123");
        req.setEmiId("emi1");
        req.setAmount(4400.0);
        req.setPaymentMethod("ONLINE");
        req.setTransactionId("txn123");

        Payment payment = new Payment();
        payment.setId("payment123");
        payment.setStatus("COMPLETED");

        when(paymentService.recordPayment("loan123", "emi1", 4400.0, "ONLINE", "txn123"))
                .thenReturn(payment);

        // Act
        ResponseEntity<Payment> result = controller.recordPayment(req);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("COMPLETED", result.getBody().getStatus());
        verify(paymentService, times(1)).recordPayment(anyString(), anyString(), 
                anyDouble(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should get payments by loan")
    void testGetPaymentsByLoan() {
        // Arrange
        String loanId = "loan123";
        Payment payment = new Payment();
        payment.setId("payment123");
        payment.setLoanId(loanId);

        when(paymentService.getPaymentsByLoan(loanId)).thenReturn(List.of(payment));

        // Act
        ResponseEntity<List<Payment>> result = controller.getPaymentsByLoan(loanId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(paymentService, times(1)).getPaymentsByLoan(loanId);
    }

    @Test
    @DisplayName("Should get payments by EMI")
    void testGetPaymentsByEmi() {
        // Arrange
        String emiId = "emi1";
        Payment payment = new Payment();
        payment.setId("payment123");
        payment.setEmiId(emiId);

        when(paymentService.getPaymentsByEmi(emiId)).thenReturn(List.of(payment));

        // Act
        ResponseEntity<List<Payment>> result = controller.getPaymentsByEmi(emiId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    @DisplayName("Should get payment by ID")
    void testGetPaymentById() {
        // Arrange
        String paymentId = "payment123";
        Payment payment = new Payment();
        payment.setId(paymentId);

        when(paymentService.getPayment(paymentId)).thenReturn(payment);

        // Act
        ResponseEntity<Payment> result = controller.getPayment(paymentId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(paymentId, result.getBody().getId());
    }
}
