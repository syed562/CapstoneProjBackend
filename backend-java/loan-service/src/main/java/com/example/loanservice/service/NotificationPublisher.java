package com.example.loanservice.service;

import com.example.loanservice.config.RabbitMQProducerConfig;
import com.example.loanservice.event.EMIEvent;
import com.example.loanservice.client.ProfileServiceClient;
import com.example.loanservice.client.dto.ProfileView;
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
    private final ProfileServiceClient profileServiceClient;

    public NotificationPublisher(RabbitTemplate rabbitTemplate, ProfileServiceClient profileServiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.profileServiceClient = profileServiceClient;
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
     * Enrich event with actual user data from profile service
     */
    private void enrichEventWithMockData(EMIEvent event) {
        if (event.getUserId() != null && (event.getUserEmail() == null || event.getUserEmail().isEmpty())) {
            try {
                ProfileView profile = profileServiceClient.getProfile(event.getUserId());
                if (profile != null) {
                    if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
                        event.setUserEmail(profile.getEmail());
                    }
                    if (event.getUserName() == null || event.getUserName().isEmpty()) {
                        String fullName = "";
                        if (profile.getFirstName() != null) {
                            fullName = profile.getFirstName();
                        }
                        if (profile.getLastName() != null) {
                            fullName = fullName.isEmpty() ? profile.getLastName() : fullName + " " + profile.getLastName();
                        }
                        if (!fullName.isEmpty()) {
                            event.setUserName(fullName);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch profile for userId {}: {}", event.getUserId(), e.getMessage());
                // Continue without email/name rather than failing the notification
            }
        }
    }
}
