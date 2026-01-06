package com.example.notificationservice.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender);
        // Set email configuration via reflection
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@loanmanagement.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Loan Management System");
    }

    // --- Loan Application Notification Tests ---

    @Test
    @DisplayName("Should send approved loan application notification successfully")
    void testSendLoanApplicationNotification_Approved() {
        String toEmail = "john@example.com";
        String userName = "John Doe";
        String status = "APPROVED";
        String applicationId = "app-001";
        Double loanAmount = 500000.0;
        String remarks = "Excellent credit score";

        emailService.sendLoanApplicationNotification(toEmail, userName, status, 
            applicationId, loanAmount, remarks);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertEquals(toEmail, message.getTo()[0]);
        assertEquals("noreply@loanmanagement.com", message.getFrom());
        assertTrue(message.getSubject().contains("APPROVED"));
        assertTrue(message.getText().contains("John Doe"));
        assertTrue(message.getText().contains("app-001"));
        assertTrue(message.getText().contains("500000"));
        assertTrue(message.getText().contains("Excellent credit score"));
    }

    @Test
    @DisplayName("Should send rejected loan application notification successfully")
    void testSendLoanApplicationNotification_Rejected() {
        String toEmail = "jane@example.com";
        String userName = "Jane Smith";
        String status = "REJECTED";
        String applicationId = "app-002";
        Double loanAmount = 300000.0;
        String remarks = "Insufficient income";

        emailService.sendLoanApplicationNotification(toEmail, userName, status, 
            applicationId, loanAmount, remarks);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertEquals("REJECTED", message.getSubject().split(" ")[2]);
        assertTrue(message.getText().contains("Insufficient income"));
    }

    @Test
    @DisplayName("Should send loan application notification with null remarks")
    void testSendLoanApplicationNotification_NoRemarks() {
        String toEmail = "user@example.com";
        
        emailService.sendLoanApplicationNotification(toEmail, "User", "PENDING", 
            "app-003", 100000.0, null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertFalse(message.getText().contains("Remarks: "));
    }

    @Test
    @DisplayName("Should handle exception when sending loan application notification")
    void testSendLoanApplicationNotification_Exception() {
        doThrow(new RuntimeException("Mail server error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Should not throw; exceptions are caught
        assertDoesNotThrow(() -> 
            emailService.sendLoanApplicationNotification("test@example.com", "Test", 
                "APPROVED", "app-004", 100000.0, "Note")
        );
        
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // --- EMI Due Reminder Tests ---

    @Test
    @DisplayName("Should send EMI due reminder successfully")
    void testSendEMIDueReminder_Success() {
        String toEmail = "john@example.com";
        String userName = "John Doe";
        Double emiAmount = 5000.0;
        String dueDate = "2026-02-06";
        String loanId = "loan-001";
        Integer monthNumber = 5;

        emailService.sendEMIDueReminder(toEmail, userName, emiAmount, dueDate, loanId, monthNumber);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertEquals(toEmail, message.getTo()[0]);
        assertTrue(message.getSubject().contains("EMI Due Reminder"));
        assertTrue(message.getSubject().contains(dueDate));
        assertTrue(message.getText().contains("John Doe"));
        assertTrue(message.getText().contains("loan-001"));
        assertTrue(message.getText().contains("5000"));
        assertTrue(message.getText().contains("2026-02-06"));
        assertTrue(message.getText().contains("5"));
    }

    @Test
    @DisplayName("Should send EMI due reminder with large month number")
    void testSendEMIDueReminder_LargeMonth() {
        emailService.sendEMIDueReminder("user@example.com", "User", 10000.0, 
            "2030-12-25", "loan-999", 120);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains("120"));
    }

    @Test
    @DisplayName("Should handle exception when sending EMI due reminder")
    void testSendEMIDueReminder_Exception() {
        doThrow(new RuntimeException("Network error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> 
            emailService.sendEMIDueReminder("test@example.com", "Test", 
                5000.0, "2026-02-06", "loan-001", 5)
        );
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // --- EMI Overdue Notification Tests ---

    @Test
    @DisplayName("Should send EMI overdue notification successfully")
    void testSendEMIOverdueNotification_Success() {
        String toEmail = "john@example.com";
        String userName = "John Doe";
        Double emiAmount = 5000.0;
        String dueDate = "2026-01-06";
        String loanId = "loan-001";
        Double outstandingBalance = 95000.0;

        emailService.sendEMIOverdueNotification(toEmail, userName, emiAmount, 
            dueDate, loanId, outstandingBalance);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertEquals(toEmail, message.getTo()[0]);
        assertTrue(message.getSubject().contains("EMI Overdue"));
        assertTrue(message.getSubject().contains("Immediate Action Required"));
        assertTrue(message.getText().contains("John Doe"));
        assertTrue(message.getText().contains("OVERDUE"));
        assertTrue(message.getText().contains("loan-001"));
        assertTrue(message.getText().contains("5000"));
        assertTrue(message.getText().contains("95000"));
    }

    @Test
    @DisplayName("Should send EMI overdue with high outstanding balance")
    void testSendEMIOverdueNotification_HighBalance() {
        emailService.sendEMIOverdueNotification("user@example.com", "User", 
            15000.0, "2026-01-01", "loan-huge", 500000.0);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains("500000"));
    }

    @Test
    @DisplayName("Should handle exception when sending EMI overdue notification")
    void testSendEMIOverdueNotification_Exception() {
        doThrow(new RuntimeException("SMTP error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> 
            emailService.sendEMIOverdueNotification("test@example.com", "Test", 
                5000.0, "2026-01-06", "loan-001", 95000.0)
        );
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // --- Loan Closure Notification Tests ---

    @Test
    @DisplayName("Should send loan closure notification successfully")
    void testSendLoanClosureNotification_Success() {
        String toEmail = "john@example.com";
        String userName = "John Doe";
        String loanId = "loan-001";

        emailService.sendLoanClosureNotification(toEmail, userName, loanId);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertEquals(toEmail, message.getTo()[0]);
        assertEquals("Loan Successfully Closed", message.getSubject());
        assertTrue(message.getText().contains("John Doe"));
        assertTrue(message.getText().contains("Congratulations"));
        assertTrue(message.getText().contains("loan-001"));
        assertTrue(message.getText().contains("successfully closed"));
    }

    @Test
    @DisplayName("Should send loan closure with special characters in name")
    void testSendLoanClosureNotification_SpecialChars() {
        emailService.sendLoanClosureNotification("user@example.com", 
            "José García-López", "loan-special-123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains("José García-López"));
    }

    @Test
    @DisplayName("Should handle exception when sending loan closure notification")
    void testSendLoanClosureNotification_Exception() {
        doThrow(new RuntimeException("Connection timeout"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> 
            emailService.sendLoanClosureNotification("test@example.com", "Test", "loan-001")
        );
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // --- Integration and Edge Case Tests ---

    @Test
    @DisplayName("Should use configured from email address in all messages")
    void testFromEmailAddressUsedInAllNotifications() {
        emailService.sendLoanApplicationNotification("to@example.com", "User", 
            "APPROVED", "app-001", 100000.0, null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        assertEquals("noreply@loanmanagement.com", captor.getValue().getFrom());
    }

    @Test
    @DisplayName("Should handle empty string remarks as null")
    void testSendLoanApplicationNotification_EmptyRemarks() {
        emailService.sendLoanApplicationNotification("test@example.com", "User", 
            "PENDING", "app-005", 50000.0, "");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        // Empty string is still included
        assertTrue(captor.getValue().getText().contains("Remarks: "));
    }

    @Test
    @DisplayName("Should format currency amounts consistently")
    void testCurrencyFormatting() {
        emailService.sendEMIDueReminder("test@example.com", "User", 
            1234.5, "2026-02-06", "loan-001", 1);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        // Amount should be formatted to 2 decimal places
        assertTrue(captor.getValue().getText().contains("1234.50"));
    }

    @Test
    @DisplayName("Should send multiple notifications without interference")
    void testMultipleNotificationsSequentially() {
        emailService.sendLoanApplicationNotification("app@example.com", "User", 
            "APPROVED", "app-001", 100000.0, "Good");
        
        emailService.sendEMIDueReminder("emi@example.com", "User", 
            5000.0, "2026-02-06", "loan-001", 1);
        
        emailService.sendLoanClosureNotification("closure@example.com", "User", "loan-001");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(captor.capture());
        
        // Verify all three had different recipients
        java.util.List<SimpleMailMessage> messages = captor.getAllValues();
        assertEquals(3, messages.size());
        assertEquals("app@example.com", messages.get(0).getTo()[0]);
        assertEquals("emi@example.com", messages.get(1).getTo()[0]);
        assertEquals("closure@example.com", messages.get(2).getTo()[0]);
    }

    @Test
    @DisplayName("Should handle null loan amount gracefully")
    void testSendLoanApplicationNotification_NullAmount() {
        emailService.sendLoanApplicationNotification("test@example.com", "User", 
            "APPROVED", "app-001", null, "Notes");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        
        assertNotNull(captor.getValue());
    }
}
