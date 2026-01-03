package com.example.loanservice.service;

import com.example.loanservice.config.RabbitMQProducerConfig;
import com.example.loanservice.event.EMIEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes EMI-related events to notification service via RabbitMQ
 * Uses mock email/name for testing - will integrate with profile-service later
 */
@Component
@Slf4j
public class NotificationPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private static final String MOCK_USER_EMAIL = "syedsabiha982@gmail.com";
    private static final String MOCK_USER_NAME = "Test Customer";
    
    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void publishEmiDue(EMIEvent event) {
        try {
            enrichEventWithMockData(event);
            event.setEventType("EMI_DUE");
            event.setTimestamp(System.currentTimeMillis());
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.EMI_EXCHANGE,
                "emi.due",
                event
            );
            log.info("Published EMI_DUE event for loan: {} to {}", event.getLoanId(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish EMI_DUE event: {}", e.getMessage(), e);
        }
    }
    
    public void publishEmiOverdue(EMIEvent event) {
        try {
            enrichEventWithMockData(event);
            event.setEventType("EMI_OVERDUE");
            event.setTimestamp(System.currentTimeMillis());
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.EMI_EXCHANGE,
                "emi.overdue",
                event
            );
            log.info("Published EMI_OVERDUE event for loan: {} to {}", event.getLoanId(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish EMI_OVERDUE event: {}", e.getMessage(), e);
        }
    }
    
    public void publishLoanClosed(EMIEvent event) {
        try {
            enrichEventWithMockData(event);
            event.setEventType("LOAN_CLOSED");
            event.setTimestamp(System.currentTimeMillis());
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.EMI_EXCHANGE,
                "loan.closed",
                event
            );
            log.info("Published LOAN_CLOSED event for loan: {} to {}", event.getLoanId(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish LOAN_CLOSED event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Enrich event with mock user data for testing
     * TODO: Replace with actual profile-service call to fetch customer email/name
     */
    private void enrichEventWithMockData(EMIEvent event) {
        if (event.getUserEmail() == null || event.getUserEmail().isEmpty()) {
            event.setUserEmail(MOCK_USER_EMAIL);
        }
        if (event.getUserName() == null || event.getUserName().isEmpty()) {
            event.setUserName(MOCK_USER_NAME);
        }
    }
}
