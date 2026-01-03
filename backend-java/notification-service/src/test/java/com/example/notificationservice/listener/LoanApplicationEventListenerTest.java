package com.example.notificationservice.listener;

import com.example.notificationservice.event.LoanApplicationEvent;
import com.example.notificationservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

        doNothing().when(emailService).sendLoanApplicationNotification(
            anyString(), anyString(), anyString(), anyString(), anyDouble(), anyString());

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendLoanApplicationNotification(
            eq("user@example.com"), eq("John Doe"), eq("SUBMITTED"), 
            eq("app123"), eq(100000.0), anyString());
    }

    @Test
    @DisplayName("Should handle loan application approved event")
    void testHandleLoanApplicationApprovedEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app123");
        event.setEventType("APPROVED");
        event.setUserEmail("user@example.com");
        event.setUserName("Jane Doe");
        event.setLoanAmount(100000.0);

        doNothing().when(emailService).sendLoanApplicationNotification(
            anyString(), anyString(), anyString(), anyString(), anyDouble(), anyString());

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendLoanApplicationNotification(
            eq("user@example.com"), eq("Jane Doe"), eq("APPROVED"), 
            eq("app123"), eq(100000.0), anyString());
    }

    @Test
    @DisplayName("Should handle loan application rejected event")
    void testHandleLoanApplicationRejectedEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app123");
        event.setEventType("REJECTED");
        event.setUserEmail("user@example.com");
        event.setUserName("Bob Smith");
        event.setLoanAmount(50000.0);
        event.setRemarks("Insufficient income");

        doNothing().when(emailService).sendLoanApplicationNotification(
            anyString(), anyString(), anyString(), anyString(), anyDouble(), anyString());

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendLoanApplicationNotification(
            eq("user@example.com"), eq("Bob Smith"), eq("REJECTED"), 
            eq("app123"), eq(50000.0), contains("Insufficient income"));
    }

    @Test
    @DisplayName("Should handle under review event")
    void testHandleUnderReviewEvent() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app456");
        event.setEventType("UNDER_REVIEW");
        event.setUserEmail("user2@example.com");
        event.setUserName("Alice Brown");
        event.setLoanAmount(200000.0);

        doNothing().when(emailService).sendLoanApplicationNotification(
            anyString(), anyString(), anyString(), anyString(), anyDouble(), anyString());

        // Act
        listener.handleLoanApplicationEvent(event);

        // Assert
        verify(emailService, times(1)).sendLoanApplicationNotification(
            eq("user2@example.com"), eq("Alice Brown"), eq("UNDER REVIEW"), 
            eq("app456"), eq(200000.0), anyString());
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
        verify(emailService, never()).sendLoanApplicationNotification(
            anyString(), anyString(), anyString(), anyString(), anyDouble(), anyString());
    }

    @Test
    @DisplayName("Should not throw exception on email service failure")
    void testHandleEmailServiceFailure() {
        // Arrange
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app789");
        event.setEventType("CREATED");
        event.setUserEmail("user@example.com");
        event.setUserName("Test User");
        event.setLoanAmount(75000.0);

        doThrow(new RuntimeException("Email service unavailable"))
            .when(emailService).sendLoanApplicationNotification(
                anyString(), anyString(), anyString(), anyString(), anyDouble(), anyString());

        // Act & Assert - Should not throw exception, just log it
        assertDoesNotThrow(() -> listener.handleLoanApplicationEvent(event));
    }
}
