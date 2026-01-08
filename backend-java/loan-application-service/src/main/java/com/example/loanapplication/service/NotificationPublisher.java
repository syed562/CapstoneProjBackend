package com.example.loanapplication.service;

import com.example.loanapplication.config.RabbitMQProducerConfig;
import com.example.loanapplication.event.LoanApplicationEvent;
import com.example.loanapplication.client.ProfileServiceClient;
import com.example.loanapplication.client.dto.ProfileView;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes loan application events to notification service via RabbitMQ
 * Fetches user email and name from profile-service
 */
@Component
@Slf4j
public class NotificationPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private final ProfileServiceClient profileServiceClient;
    
    public NotificationPublisher(RabbitTemplate rabbitTemplate, ProfileServiceClient profileServiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.profileServiceClient = profileServiceClient;
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
     * Enrich event with actual user data from profile service
     */
    private static final String DEFAULT_EMAIL = "syedsabiha982@gmail.com";
    private void enrichEventWithMockData(LoanApplicationEvent event) {
        log.info("[NOTIFY] Enriching event for userId: {} (current email: {})", event.getUserId(), event.getUserEmail());
        if (event.getUserId() != null) {
            try {
                ProfileView profile = profileServiceClient.getProfile(event.getUserId());
                log.info("[NOTIFY] Profile fetched for userId {}: {}", event.getUserId(), profile);
                if (profile != null && profile.getEmail() != null && !profile.getEmail().isEmpty()) {
                    event.setUserEmail(profile.getEmail());
                    log.info("[NOTIFY] Set event email to profile email: {}", profile.getEmail());
                } else {
                    event.setUserEmail(DEFAULT_EMAIL);
                    log.warn("[NOTIFY] Profile email missing, set to default: {}", DEFAULT_EMAIL);
                }
                if (event.getUserName() == null || event.getUserName().isEmpty()) {
                    String fullName = "";
                    if (profile != null && profile.getFirstName() != null) {
                        fullName = profile.getFirstName();
                    }
                    if (profile != null && profile.getLastName() != null) {
                        fullName = fullName.isEmpty() ? profile.getLastName() : fullName + " " + profile.getLastName();
                    }
                    if (!fullName.isEmpty()) {
                        event.setUserName(fullName);
                        log.info("[NOTIFY] Set event userName to: {}", fullName);
                    }
                }
            } catch (Exception e) {
                log.warn("[NOTIFY] Failed to fetch profile for userId {}: {}", event.getUserId(), e.getMessage());
                event.setUserEmail(DEFAULT_EMAIL);
            }
        } else if (event.getUserEmail() == null || event.getUserEmail().isEmpty()) {
            event.setUserEmail(DEFAULT_EMAIL);
            log.warn("[NOTIFY] userId missing, set event email to default: {}", DEFAULT_EMAIL);
        }
        log.info("[NOTIFY] Final event email: {}", event.getUserEmail());
    }
}
