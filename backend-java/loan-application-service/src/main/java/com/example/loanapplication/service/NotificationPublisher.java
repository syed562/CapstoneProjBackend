package com.example.loanapplication.service;

import com.example.loanapplication.config.RabbitMQProducerConfig;
import com.example.loanapplication.event.LoanApplicationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes loan application events to notification service via RabbitMQ
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
    
    /**
     * Publish loan application created event
     */
    public void publishApplicationCreated(LoanApplicationEvent event) {
        try {
            enrichEventWithMockData(event);
            event.setEventType("CREATED");
            event.setTimestamp(System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.LOAN_APPLICATION_EXCHANGE,
                "loan.application.created",
                event
            );
            log.info("Published CREATED event for application: {} to {}", event.getApplicationId(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish application created event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish loan application under review event
     */
    public void publishApplicationUnderReview(LoanApplicationEvent event) {
        try {
            enrichEventWithMockData(event);
            event.setEventType("UNDER_REVIEW");
            event.setTimestamp(System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.LOAN_APPLICATION_EXCHANGE,
                "loan.application.under_review",
                event
            );
            log.info("Published UNDER_REVIEW event for application: {} to {}", event.getApplicationId(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish application under review event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish loan application approved event
     */
    public void publishApplicationApproved(LoanApplicationEvent event) {
        try {
            enrichEventWithMockData(event);
            event.setEventType("APPROVED");
            event.setTimestamp(System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.LOAN_APPLICATION_EXCHANGE,
                "loan.application.approved",
                event
            );
            log.info("Published APPROVED event for application: {} to {}", event.getApplicationId(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish application approved event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish loan application rejected event
     */
    public void publishApplicationRejected(LoanApplicationEvent event) {
        try {
            enrichEventWithMockData(event);
            event.setEventType("REJECTED");
            event.setTimestamp(System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.LOAN_APPLICATION_EXCHANGE,
                "loan.application.rejected",
                event
            );
            log.info("Published REJECTED event for application: {} to {}", event.getApplicationId(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish application rejected event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Enrich event with mock user data for testing
     * TODO: Replace with actual profile-service call to fetch customer email/name
     */
    private void enrichEventWithMockData(LoanApplicationEvent event) {
        if (event.getUserEmail() == null || event.getUserEmail().isEmpty()) {
            event.setUserEmail(MOCK_USER_EMAIL);
        }
        if (event.getUserName() == null || event.getUserName().isEmpty()) {
            event.setUserName(MOCK_USER_NAME);
        }
    }
}
