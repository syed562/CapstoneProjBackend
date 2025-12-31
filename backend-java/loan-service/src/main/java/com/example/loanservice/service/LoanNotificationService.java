package com.example.loanservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.loanservice.emi.EMICalculator;

/**
 * Service to handle loan-related notifications
 */
@Service
public class LoanNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(LoanNotificationService.class);

    /**
     * Send EMI notification to customer after loan approval
     */
    public void sendEMINotification(String userId, String loanId, double amount, 
                                   double interestRate, int termMonths) {
        try {
            double emiAmount = EMICalculator.calculateEMI(amount, interestRate, termMonths);
            double totalPayable = emiAmount * termMonths;
            double totalInterest = totalPayable - amount;
            
            String message = String.format(
                "EMI Schedule Generated Successfully!\n" +
                "Loan ID: %s\n" +
                "Loan Amount: %.2f\n" +
                "Interest Rate: %.2f%%\n" +
                "Tenure: %d months\n" +
                "Monthly EMI: %.2f\n" +
                "Total Interest: %.2f\n" +
                "Total Payable: %.2f\n" +
                "Your first payment is due next month. Please ensure timely payments to maintain good credit standing.",
                loanId, amount, interestRate, termMonths, emiAmount, totalInterest, totalPayable
            );
            
            logger.info("[LOAN NOTIFICATION] EMI Schedule - User: {}, Loan: {}, EMI Amount: {}", 
                userId, loanId, emiAmount);
            logger.info("[LOAN NOTIFICATION MESSAGE]\n{}", message);
            
            // TODO: Implement actual email/SMS notification
            // Example: emailService.sendEmail(userEmail, "EMI Schedule Generated", message);
            
        } catch (Exception e) {
            logger.error("Error sending EMI notification for user: {} loan: {}", userId, loanId, e);
        }
    }

    /**
     * Send payment reminder notification
     */
    public void sendPaymentReminder(String userId, String loanId, double emiAmount, String dueDate) {
        try {
            String message = String.format(
                "Payment Reminder\n" +
                "Loan ID: %s\n" +
                "EMI Amount Due: %.2f\n" +
                "Due Date: %s\n" +
                "Please make the payment on or before the due date to avoid penalties.",
                loanId, emiAmount, dueDate
            );
            
            logger.info("[LOAN NOTIFICATION] Payment Reminder - User: {}, Loan: {}, Amount: {}", 
                userId, loanId, emiAmount);
            logger.info("[LOAN NOTIFICATION MESSAGE]\n{}", message);
            
        } catch (Exception e) {
            logger.error("Error sending payment reminder for user: {} loan: {}", userId, loanId, e);
        }
    }

    /**
     * Send payment confirmation notification
     */
    public void sendPaymentConfirmation(String userId, String loanId, double amount, 
                                       String transactionId, Double outstandingBalance) {
        try {
            String message = String.format(
                "Payment Received Successfully!\n" +
                "Loan ID: %s\n" +
                "Amount Paid: %.2f\n" +
                "Transaction ID: %s\n" +
                "Outstanding Balance: %.2f\n" +
                "Thank you for your timely payment!",
                loanId, amount, transactionId, outstandingBalance
            );
            
            logger.info("[LOAN NOTIFICATION] Payment Confirmation - User: {}, Loan: {}, Amount: {}", 
                userId, loanId, amount);
            logger.info("[LOAN NOTIFICATION MESSAGE]\n{}", message);
            
        } catch (Exception e) {
            logger.error("Error sending payment confirmation for user: {} loan: {}", userId, loanId, e);
        }
    }
}
