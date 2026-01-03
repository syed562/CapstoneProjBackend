package com.example.notificationservice.listener;

import com.example.notificationservice.event.LoanApplicationEvent;
import com.example.notificationservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

@DisplayName("LoanApplicationEventListener Tests")
class LoanApplicationEventListenerTest {

    @Mock
    private EmailService emailService;

    private LoanApplicationEventListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new LoanApplicationEventListener(emailService);
    }

    @Test
    @DisplayName("Should handle loan application created event")
    void testHandleLoanApplicationCreatedEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app123");
        event.setUserId("user123");
        event.setUserEmail("user@example.com");
        event.setUserName("John Doe");
        event.setLoanAmount(100000.0);
        event.setEventType("CREATED");

        doNothing().when(emailService).sendLoanCreatedMail(event);

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendLoanCreatedMail(event);
    }

    @Test
    @DisplayName("Should handle loan application approved event")
    void testHandleLoanApplicationApprovedEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app123");
        event.setEventType("APPROVED");
        event.setUserEmail("user@example.com");
        event.setLoanAmount(100000.0);

        doNothing().when(emailService).sendApprovalMail(event);

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendApprovalMail(event);
    }

    @Test
    @DisplayName("Should handle loan application rejected event")
    void testHandleLoanApplicationRejectedEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app123");
        event.setEventType("REJECTED");
        event.setUserEmail("user@example.com");
        event.setRemarks("Insufficient income");

        doNothing().when(emailService).sendRejectionMail(event);

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendRejectionMail(event);
    }

    @Test
    @DisplayName("Should handle EMI due event")
    void testHandleEmiDueEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setEventType("EMI_DUE");
        event.setUserEmail("user@example.com");
        event.setUserName("John Doe");
        event.setLoanAmount(4400.0);

        doNothing().when(emailService).sendEmiDueReminder(event);

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendEmiDueReminder(event);
    }

    @Test
    @DisplayName("Should handle EMI overdue event")
    void testHandleEmiOverdueEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setEventType("EMI_OVERDUE");
        event.setUserEmail("user@example.com");
        event.setLoanAmount(4400.0);

        doNothing().when(emailService).sendEmiOverdueAlert(event);

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendEmiOverdueAlert(event);
    }

    @Test
    @DisplayName("Should handle loan closed event")
    void testHandleLoanClosedEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setEventType("LOAN_CLOSED");
        event.setUserEmail("user@example.com");
        event.setUserName("John Doe");
        event.setLoanAmount(100000.0);

        doNothing().when(emailService).sendLoanClosureMail(event);

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendLoanClosureMail(event);
    }

    @Test
    @DisplayName("Should handle unknown event type gracefully")
    void testHandleUnknownEventType() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setEventType("UNKNOWN");
        event.setUserEmail("user@example.com");

        // Act - Should not throw exception
        assertDoesNotThrow(() -> listener.handleLoanApplicationEvent(event));

        // Assert - No email should be sent for unknown event
        verify(emailService, never()).sendLoanCreatedMail(any());
    }
}
