package com.example.loanservice.service;

import com.example.loanservice.config.RabbitMQProducerConfig;
import com.example.loanservice.event.EMIEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes EMI and loan events to notification service via RabbitMQ
 */
@Component
@Slf4j
public class EMINotificationPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public EMINotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Publish EMI due reminder event (send 7 days before due date)
     */
    public void publishEMIDueReminder(EMIEvent event) {
        try {
            event.setEventType("EMI_DUE");
            event.setTimestamp(System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.EMI_EXCHANGE,
                "emi.due",
                event
            );
            log.info("Published EMI DUE reminder for loan: {} month: {}", event.getLoanId(), event.getMonthNumber());
        } catch (Exception e) {
            log.error("Failed to publish EMI due event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish EMI overdue alert event (sent when payment is past due date)
     */
    public void publishEMIOverdueAlert(EMIEvent event) {
        try {
            event.setEventType("EMI_OVERDUE");
            event.setTimestamp(System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.EMI_EXCHANGE,
                "emi.overdue",
                event
            );
            log.info("Published EMI OVERDUE alert for loan: {} month: {}", event.getLoanId(), event.getMonthNumber());
        } catch (Exception e) {
            log.error("Failed to publish EMI overdue event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish loan closure event (sent when final EMI is paid)
     */
    public void publishLoanClosure(EMIEvent event) {
        try {
            event.setEventType("LOAN_CLOSED");
            event.setTimestamp(System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.EMI_EXCHANGE,
                "loan.closed",
                event
            );
            log.info("Published LOAN CLOSURE notification for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to publish loan closure event: {}", e.getMessage(), e);
        }
    }
}
