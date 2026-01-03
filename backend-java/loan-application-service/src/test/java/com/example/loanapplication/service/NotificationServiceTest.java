package com.example.loanapplication.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceTest {

    private final NotificationService service = new NotificationService();

    @Test
    void sendApprovalNotification_executes() {
        assertDoesNotThrow(() ->
                service.sendApprovalNotification(
                        "user1",
                        "app1",
                        50000.0,
                        24,
                        12.0,
                        "PERSONAL"
                )
        );
    }

    @Test
    void sendRejectionNotification_executes() {
        assertDoesNotThrow(() ->
                service.sendRejectionNotification(
                        "user1",
                        "app2",
                        "Insufficient income"
                )
        );
    }

    @Test
    void sendEMINotification_executes() {
        assertDoesNotThrow(() ->
                service.sendEMINotification(
                        "user1",
                        "app3",
                        2500,
                        12.5
                )
        );
    }
}
