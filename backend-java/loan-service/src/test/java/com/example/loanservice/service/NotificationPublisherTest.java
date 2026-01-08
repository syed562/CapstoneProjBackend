package com.example.loanservice.service;

import com.example.loanservice.client.ProfileServiceClient;
import com.example.loanservice.event.EMIEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class NotificationPublisherTest {

    private RabbitTemplate rabbitTemplate;
    private ProfileServiceClient profileServiceClient;
    private NotificationPublisher publisher;

    @BeforeEach
    void setup() {
        rabbitTemplate = mock(RabbitTemplate.class);
        profileServiceClient = mock(ProfileServiceClient.class);
        publisher = new NotificationPublisher(rabbitTemplate, profileServiceClient);
    }

    @Test
    void publishEmiDue_shouldNotThrow() {
        EMIEvent event = new EMIEvent();
        event.setLoanId("loan1");

        assertDoesNotThrow(() -> publisher.publishEmiDue(event));
    }

    @Test
    void publishEmiOverdue_shouldNotThrow() {
        EMIEvent event = new EMIEvent();
        event.setLoanId("loan2");

        assertDoesNotThrow(() -> publisher.publishEmiOverdue(event));
    }

    @Test
    void publishLoanClosed_shouldNotThrow() {
        EMIEvent event = new EMIEvent();
        event.setLoanId("loan3");

        assertDoesNotThrow(() -> publisher.publishLoanClosed(event));
    }
}
