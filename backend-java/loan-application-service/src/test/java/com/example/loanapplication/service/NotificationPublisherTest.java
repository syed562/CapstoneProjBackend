package com.example.loanapplication.service;

import com.example.loanapplication.event.LoanApplicationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationPublisherTest {

    private NotificationPublisher publisher;
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        publisher = new NotificationPublisher(rabbitTemplate);
    }

    @Test
    void publishApplicationCreated_eventWithData_executes() {
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app1");
        event.setUserId("user1");
        event.setLoanAmount(50000.0);

        assertDoesNotThrow(() ->
                publisher.publishApplicationCreated(event)
        );

        verify(rabbitTemplate, atLeastOnce())
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class)
                );
    }

    @Test
    void publishApplicationApproved_eventWithNullFields_executes() {
        LoanApplicationEvent event = new LoanApplicationEvent();
        // intentionally null fields

        assertDoesNotThrow(() ->
                publisher.publishApplicationApproved(event)
        );

        verify(rabbitTemplate, atLeastOnce())
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class)
                );
    }

    @Test
    void publishApplicationRejected_eventWithPartialData_executes() {
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app3");

        assertDoesNotThrow(() ->
                publisher.publishApplicationRejected(event)
        );

        verify(rabbitTemplate, atLeastOnce())
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class)
                );
    }

    @Test
    void publishApplicationUnderReview_executes() {
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setApplicationId("app4");
        event.setUserId("user4");

        assertDoesNotThrow(() ->
                publisher.publishApplicationUnderReview(event)
        );

        verify(rabbitTemplate, atLeastOnce())
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class)
                );
    }

    @Test
    void enrichEventWithMockData_allNullFields_executesBranches() {
        LoanApplicationEvent event = new LoanApplicationEvent();

        assertDoesNotThrow(() ->
                publisher.publishApplicationCreated(event)
        );
    }
}
