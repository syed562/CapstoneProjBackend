package com.example.notificationservice.listener;

import com.example.notificationservice.config.RabbitMQConfig;
import com.example.notificationservice.event.LoanApplicationEvent;
import com.example.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoanApplicationEventListener {
    
    private final EmailService emailService;
    
    public LoanApplicationEventListener(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @RabbitListener(queues = RabbitMQConfig.LOAN_APPLICATION_QUEUE)
    public void handleLoanApplicationEvent(LoanApplicationEvent event) {
        log.info("Received loan application event: {}", event.getEventType());
        
        try {
            switch (event.getEventType()) {
                case "CREATED":
                    emailService.sendLoanApplicationNotification(
                        event.getUserEmail(),
                        event.getUserName(),
                        "SUBMITTED",
                        event.getApplicationId(),
                        event.getLoanAmount(),
                        "Your loan application has been submitted successfully. " +
                        "Our loan officers will review it within 2-3 business days."
                    );
                    break;
                    
                case "APPROVED":
                    emailService.sendLoanApplicationNotification(
                        event.getUserEmail(),
                        event.getUserName(),
                        "APPROVED",
                        event.getApplicationId(),
                        event.getLoanAmount(),
                        "Congratulations! Your loan application has been approved. " +
                        "The amount will be disbursed to your account within 1-2 business days."
                    );
                    break;
                    
                case "REJECTED":
                    emailService.sendLoanApplicationNotification(
                        event.getUserEmail(),
                        event.getUserName(),
                        "REJECTED",
                        event.getApplicationId(),
                        event.getLoanAmount(),
                        "Reason: " + event.getRemarks()
                    );
                    break;
                    
                case "UNDER_REVIEW":
                    emailService.sendLoanApplicationNotification(
                        event.getUserEmail(),
                        event.getUserName(),
                        "UNDER REVIEW",
                        event.getApplicationId(),
                        event.getLoanAmount(),
                        "Your application is being reviewed by our loan officers."
                    );
                    break;
                    
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing loan application event: {}", e.getMessage(), e);
        }
    }
}
