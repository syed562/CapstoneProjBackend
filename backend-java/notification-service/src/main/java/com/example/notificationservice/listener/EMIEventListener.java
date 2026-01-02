package com.example.notificationservice.listener;

import com.example.notificationservice.config.RabbitMQConfig;
import com.example.notificationservice.event.EMIEvent;
import com.example.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EMIEventListener {
    
    private final EmailService emailService;
    
    public EMIEventListener(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @RabbitListener(queues = RabbitMQConfig.EMI_DUE_QUEUE)
    public void handleEMIDueEvent(EMIEvent event) {
        log.info("Received EMI due event for loan: {}", event.getLoanId());
        
        try {
            emailService.sendEMIDueReminder(
                event.getUserEmail(),
                event.getUserName(),
                event.getEmiAmount(),
                event.getDueDate().toString(),
                event.getLoanId(),
                event.getMonthNumber()
            );
        } catch (Exception e) {
            log.error("Error processing EMI due event: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.EMI_OVERDUE_QUEUE)
    public void handleEMIOverdueEvent(EMIEvent event) {
        log.info("Received EMI overdue event for loan: {}", event.getLoanId());
        
        try {
            emailService.sendEMIOverdueNotification(
                event.getUserEmail(),
                event.getUserName(),
                event.getEmiAmount(),
                event.getDueDate().toString(),
                event.getLoanId(),
                event.getOutstandingBalance()
            );
        } catch (Exception e) {
            log.error("Error processing EMI overdue event: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.LOAN_CLOSURE_QUEUE)
    public void handleLoanClosureEvent(EMIEvent event) {
        log.info("Received loan closure event for loan: {}", event.getLoanId());
        
        try {
            emailService.sendLoanClosureNotification(
                event.getUserEmail(),
                event.getUserName(),
                event.getLoanId()
            );
        } catch (Exception e) {
            log.error("Error processing loan closure event: {}", e.getMessage(), e);
        }
    }
}
