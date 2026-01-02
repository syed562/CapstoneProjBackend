package com.example.notificationservice.publisher;

import com.example.notificationservice.event.LoanApplicationEvent;
import com.example.notificationservice.event.EMIEvent;
import com.example.notificationservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Publisher utility for other services to publish events
 * This can be used by loan-application-service and loan-service
 */
@Component
@Slf4j
public class NotificationEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public NotificationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Publish loan application event
     */
    public void publishLoanApplicationEvent(LoanApplicationEvent event) {
        try {
            String routingKey = "loan.application." + event.getEventType().toLowerCase();
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.LOAN_APPLICATION_EXCHANGE,
                routingKey,
                event
            );
            log.info("Published loan application event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish loan application event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish EMI due event
     */
    public void publishEMIDueEvent(EMIEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMI_EXCHANGE,
                "emi.due",
                event
            );
            log.info("Published EMI due event for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to publish EMI due event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish EMI overdue event
     */
    public void publishEMIOverdueEvent(EMIEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMI_EXCHANGE,
                "emi.overdue",
                event
            );
            log.info("Published EMI overdue event for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to publish EMI overdue event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish loan closure event
     */
    public void publishLoanClosureEvent(EMIEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMI_EXCHANGE,
                "loan.closed",
                event
            );
            log.info("Published loan closure event for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to publish loan closure event: {}", e.getMessage(), e);
        }
    }
}
