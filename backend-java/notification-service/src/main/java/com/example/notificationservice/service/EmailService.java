package com.example.notificationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${notification.mail.from}")
    private String fromEmail;
    
    @Value("${notification.mail.from-name}")
    private String fromName;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Send loan application status notification
     */
    public void sendLoanApplicationNotification(String toEmail, String userName, 
                                               String status, String applicationId, 
                                               Double loanAmount, String remarks) {
        try {
            String subject = "Loan Application " + status;
            String body = buildLoanApplicationEmail(userName, status, applicationId, loanAmount, remarks);
            
            sendEmail(toEmail, subject, body);
            log.info("Loan application notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send loan application notification to {}: {}", toEmail, e.getMessage());
        }
    }
    
    /**
     * Send EMI due reminder
     */
    public void sendEMIDueReminder(String toEmail, String userName, Double emiAmount, 
                                   String dueDate, String loanId, Integer monthNumber) {
        try {
            String subject = "EMI Due Reminder - " + dueDate;
            String body = buildEMIDueReminderEmail(userName, emiAmount, dueDate, loanId, monthNumber);
            
            sendEmail(toEmail, subject, body);
            log.info("EMI due reminder sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send EMI due reminder to {}: {}", toEmail, e.getMessage());
        }
    }
    
    /**
     * Send EMI overdue notification
     */
    public void sendEMIOverdueNotification(String toEmail, String userName, Double emiAmount, 
                                          String dueDate, String loanId, Double outstandingBalance) {
        try {
            String subject = "⚠️ EMI Overdue - Immediate Action Required";
            String body = buildEMIOverdueEmail(userName, emiAmount, dueDate, loanId, outstandingBalance);
            
            sendEmail(toEmail, subject, body);
            log.info("EMI overdue notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send EMI overdue notification to {}: {}", toEmail, e.getMessage());
        }
    }
    
    /**
     * Send loan closure notification
     */
    public void sendLoanClosureNotification(String toEmail, String userName, String loanId) {
        try {
            String subject = "Loan Successfully Closed";
            String body = buildLoanClosureEmail(userName, loanId);
            
            sendEmail(toEmail, subject, body);
            log.info("Loan closure notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send loan closure notification to {}: {}", toEmail, e.getMessage());
        }
    }
    
    /**
     * Generic email sending method
     */
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(fromEmail);
        message.setSubject(subject);
        message.setText(body);
        
        mailSender.send(message);
    }
    
    // Email template builders
    private String buildLoanApplicationEmail(String userName, String status, 
                                            String applicationId, Double loanAmount, String remarks) {
        return String.format(
            "Dear %s,\n\n" +
            "Your loan application has been %s.\n\n" +
            "Application ID: %s\n" +
            "Loan Amount: ₹%.2f\n" +
            "%s" +
            "\nThank you for using our Loan Management System.\n" +
            "Best regards,\n" +
            "Loan Management System Team",
            userName,
            status,
            applicationId,
            loanAmount,
            remarks != null ? "Remarks: " + remarks + "\n" : ""
        );
    }
    
    private String buildEMIDueReminderEmail(String userName, Double emiAmount, 
                                           String dueDate, String loanId, Integer monthNumber) {
        return String.format(
            "Dear %s,\n\n" +
            "This is a reminder that your EMI payment is due.\n\n" +
            "Loan ID: %s\n" +
            "Month: %d\n" +
            "EMI Amount: ₹%.2f\n" +
            "Due Date: %s\n\n" +
            "Please make the payment before the due date to avoid penalties.\n\n" +
            "Thank you,\n" +
            "Loan Management System Team",
            userName,
            loanId,
            monthNumber,
            emiAmount,
            dueDate
        );
    }
    
    private String buildEMIOverdueEmail(String userName, Double emiAmount, 
                                       String dueDate, String loanId, Double outstandingBalance) {
        return String.format(
            "Dear %s,\n\n" +
            "⚠️ IMPORTANT: Your EMI payment is OVERDUE\n\n" +
            "Loan ID: %s\n" +
            "Overdue Amount: ₹%.2f\n" +
            "Due Date Was: %s\n" +
            "Outstanding Balance: ₹%.2f\n\n" +
            "Please make the payment immediately to avoid penalties and legal action.\n\n" +
            "Contact our support team for assistance.\n\n" +
            "Loan Management System Team",
            userName,
            loanId,
            emiAmount,
            dueDate,
            outstandingBalance
        );
    }
    
    private String buildLoanClosureEmail(String userName, String loanId) {
        return String.format(
            "Dear %s,\n\n" +
            "Congratulations! Your loan has been successfully closed.\n\n" +
            "Loan ID: %s\n\n" +
            "Thank you for choosing our Loan Management System.\n" +
            "We appreciate your business.\n\n" +
            "Best regards,\n" +
            "Loan Management System Team",
            userName,
            loanId
        );
    }
}
