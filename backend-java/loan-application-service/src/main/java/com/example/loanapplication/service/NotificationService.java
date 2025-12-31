package com.example.loanapplication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to handle loan approval notifications
 * Currently logs notifications (can be extended to send email/SMS)
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Send approval notification to customer
     */
    public void sendApprovalNotification(String userId, String applicationId, double loanAmount, 
                                        int termMonths, double ratePercent, String loanType) {
        try {
            String message = String.format(
                "Loan Application Approved!\n" +
                "Application ID: %s\n" +
                "Loan Type: %s\n" +
                "Amount: %.2f\n" +
                "Tenure: %d months\n" +
                "Interest Rate: %.2f%%\n" +
                "Your EMI schedule has been generated. Please login to view details.",
                applicationId, loanType, loanAmount, termMonths, ratePercent
            );
            
            logger.info("[NOTIFICATION] Loan Approval - User: {}, Application: {}, Amount: {}", 
                userId, applicationId, loanAmount);
            logger.info("[NOTIFICATION MESSAGE]\n{}", message);
            
            // TODO: Implement actual email/SMS notification
            // Example: emailService.sendEmail(userEmail, "Loan Approved", message);
            
        } catch (Exception e) {
            logger.error("Error sending approval notification for user: {} application: {}", userId, applicationId, e);
            // Don't throw - notification failure shouldn't block approval
        }
    }

    /**
     * Send rejection notification to customer
     */
    public void sendRejectionNotification(String userId, String applicationId, String rejectionReason) {
        try {
            String message = String.format(
                "Loan Application Rejected\n" +
                "Application ID: %s\n" +
                "Reason: %s\n" +
                "Please contact our support team for more details or to reapply.",
                applicationId, rejectionReason
            );
            
            logger.info("[NOTIFICATION] Loan Rejection - User: {}, Application: {}", userId, applicationId);
            logger.info("[NOTIFICATION MESSAGE]\n{}", message);
            
            // TODO: Implement actual email/SMS notification
            
        } catch (Exception e) {
            logger.error("Error sending rejection notification for user: {} application: {}", userId, applicationId, e);
        }
    }

    /**
     * Send EMI schedule notification
     */
    public void sendEMINotification(String userId, String loanId, int totalMonths, double emiAmount) {
        try {
            String message = String.format(
                "EMI Schedule Generated\n" +
                "Loan ID: %s\n" +
                "Total Installments: %d\n" +
                "Monthly EMI Amount: %.2f\n" +
                "Your first payment is due next month. Please make timely payments to maintain good credit.",
                loanId, totalMonths, emiAmount
            );
            
            logger.info("[NOTIFICATION] EMI Schedule - User: {}, Loan: {}, EMI: {}", userId, loanId, emiAmount);
            logger.info("[NOTIFICATION MESSAGE]\n{}", message);
            
        } catch (Exception e) {
            logger.error("Error sending EMI notification for user: {} loan: {}", userId, loanId, e);
        }
    }
}
