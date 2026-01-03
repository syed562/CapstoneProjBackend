package com.example.loanservice.service;

import com.example.loanservice.event.EMIEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class EMINotificationPublisherTest {

    @Test
    void publishMethods_shouldNotThrow() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        EMINotificationPublisher publisher = new EMINotificationPublisher(rabbitTemplate);

        EMIEvent event = new EMIEvent();
        event.setLoanId("loan1");

        assertDoesNotThrow(() -> publisher.publishEMIDueReminder(event));
        assertDoesNotThrow(() -> publisher.publishEMIOverdueAlert(event));
        assertDoesNotThrow(() -> publisher.publishLoanClosure(event));
    }
}
