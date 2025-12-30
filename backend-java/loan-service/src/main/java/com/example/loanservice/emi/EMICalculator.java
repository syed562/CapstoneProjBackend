package com.example.loanservice.emi;

public class EMICalculator {

    /**
     * Calculates monthly EMI using the standard formula:
     * EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
     * Where:
     * P = Principal (loan amount)
     * r = Monthly interest rate (annual rate / 12 / 100)
     * n = Number of months
     */
    public static double calculateEMI(double principal, double annualRatePercent, int termMonths) {
        if (principal <= 0 || termMonths <= 0) {
            throw new IllegalArgumentException("Principal and term months must be positive");
        }

        if (annualRatePercent == 0) {
            return principal / termMonths;
        }

        double monthlyRate = annualRatePercent / 12 / 100;
        double numerator = principal * monthlyRate * Math.pow(1 + monthlyRate, termMonths);
        double denominator = Math.pow(1 + monthlyRate, termMonths) - 1;
        return numerator / denominator;
    }

    /**
     * Calculates the outstanding balance after n payments
     */
    public static double calculateOutstandingBalance(
            double principal,
            double annualRatePercent,
            int termMonths,
            int paymentsCompleted
    ) {
        double monthlyRate = annualRatePercent / 12 / 100;
        double emi = calculateEMI(principal, annualRatePercent, termMonths);

        if (monthlyRate == 0) {
            return Math.max(0, principal - (emi * paymentsCompleted));
        }

        double outstandingMonths = termMonths - paymentsCompleted;
        if (outstandingMonths <= 0) return 0;

        double numerator = emi * (Math.pow(1 + monthlyRate, outstandingMonths) - 1);
        double denominator = monthlyRate * Math.pow(1 + monthlyRate, outstandingMonths);
        return numerator / denominator;
    }
}
