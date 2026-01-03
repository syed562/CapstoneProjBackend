package com.example.loanservice.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoanNotificationServiceTest {

    private final LoanNotificationService service =
            new LoanNotificationService();

    @Test
    void sendEMINotification_shouldNotThrow() {
        assertDoesNotThrow(() ->
                service.sendEMINotification(
                        "user1",
                        "loan1",
                        100000,
                        10.5,
                        24
                )
        );
    }

    @Test
    void sendPaymentReminder_shouldNotThrow() {
        assertDoesNotThrow(() ->
                service.sendPaymentReminder(
                        "user1",
                        "loan1",
                        5000,
                        "2025-01-01"
                )
        );
    }

    @Test
    void sendPaymentConfirmation_shouldNotThrow() {
        assertDoesNotThrow(() ->
                service.sendPaymentConfirmation(
                        "user1",
                        "loan1",
                        5000,
                        "TXN123",
                        95000.0
                )
        );
    }
}
