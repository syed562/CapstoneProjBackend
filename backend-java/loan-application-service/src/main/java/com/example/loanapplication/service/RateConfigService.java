package com.example.loanapplication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RateConfigService {
    private final Map<String, Double> defaultRates;
    private final Map<String, Double> currentRates;

    public RateConfigService(
            @Value("${loan.rules.rates:PERSONAL=12,HOME=8.5,AUTO=10,EDUCATIONAL=7.5,HOME_LOAN=8.5}") String rateOptions
    ) {
        this.defaultRates = parseRateMap(rateOptions);
        this.currentRates = new HashMap<>(this.defaultRates); // Copy to current for modification
    }

    /**
     * Get all available loan types and their current rates
     */
    public Map<String, Double> getAllRates() {
        return new HashMap<>(currentRates);
    }

    /**
     * Get rate for a specific loan type
     */
    public Double getRate(String loanType) {
        return currentRates.getOrDefault(loanType.toUpperCase(), defaultRates.getOrDefault(loanType.toUpperCase(), 12.0));
    }

    /**
     * Update rate for a loan type
     */
    public void updateRate(String loanType, Double rate) {
        if (rate == null || rate <= 0) {
            throw new IllegalArgumentException("Rate must be a positive number");
        }
        currentRates.put(loanType.toUpperCase(), rate);
    }

    /**
     * Reset all rates to defaults
     */
    public void resetToDefaults() {
        currentRates.clear();
        currentRates.putAll(defaultRates);
    }

    /**
     * Parse rate configuration string format: PERSONAL=12,HOME=8.5,AUTO=10
     */
    private Map<String, Double> parseRateMap(String rateString) {
        Map<String, Double> rates = new HashMap<>();
        if (rateString == null || rateString.isEmpty()) {
            return rates;
        }

        for (String pair : rateString.split(",")) {
            if (pair.contains("=")) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    try {
                        rates.put(parts[0].trim().toUpperCase(), Double.parseDouble(parts[1].trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid rate value: " + parts[1]);
                    }
                }
            }
        }
        return rates;
    }
}
