package com.example.loanservice.emi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EMICalculatorTest {

    @Test
    void shouldCalculateEmiWithInterest() {
        double emi = EMICalculator.calculateEMI(100000, 12, 12);
        assertTrue(emi > 0);
    }

    @Test
    void shouldCalculateEmiWithoutInterest() {
        double emi = EMICalculator.calculateEMI(120000, 0, 12);
        assertEquals(10000, emi);
    }

    @Test
    void shouldThrowExceptionForInvalidPrincipal() {
        assertThrows(IllegalArgumentException.class, () ->
                EMICalculator.calculateEMI(0, 12, 12)
        );
    }

    @Test
    void shouldThrowExceptionForInvalidTerm() {
        assertThrows(IllegalArgumentException.class, () ->
                EMICalculator.calculateEMI(10000, 12, 0)
        );
    }

    @Test
    void shouldCalculateOutstandingBalanceWithZeroInterest() {
        double balance = EMICalculator.calculateOutstandingBalance(
                120000, 0, 12, 6
        );
        assertEquals(60000, balance);
    }

    @Test
    void shouldReturnZeroOutstandingWhenAllPaymentsDone() {
        double balance = EMICalculator.calculateOutstandingBalance(
                100000, 10, 12, 12
        );
        assertEquals(0, balance);
    }
}
