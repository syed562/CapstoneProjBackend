package com.example.loanapplication.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateConfigServiceTest {

    @Test
    @DisplayName("Should parse provided rates on construction")
    void parsesRatesFromConfigString() {
        RateConfigService service = new RateConfigService("PERSONAL=12,HOME=8.5,AUTO=10");

        Map<String, Double> rates = service.getAllRates();

        assertEquals(3, rates.size());
        assertEquals(12.0, rates.get("PERSONAL"));
        assertEquals(8.5, rates.get("HOME"));
        assertEquals(10.0, rates.get("AUTO"));
    }

    @Test
    @DisplayName("getAllRates returns a defensive copy")
    void getAllRatesReturnsCopy() {
        RateConfigService service = new RateConfigService("PERSONAL=12");

        Map<String, Double> snapshot = service.getAllRates();
        snapshot.put("NEW", 1.0); // mutate returned map

        assertEquals(12.0, service.getRate("PERSONAL"));
        assertEquals(12.0, service.getRate("NEW")); // service state unchanged
    }

    @Test
    @DisplayName("updateRate stores and getRate returns case-insensitively")
    void updateRateOverridesAndIsCaseInsensitive() {
        RateConfigService service = new RateConfigService("PERSONAL=12");

        service.updateRate("personal", 15.0);

        assertEquals(15.0, service.getRate("PERSONAL"));
        assertEquals(15.0, service.getRate("personal"));
    }

    @Test
    @DisplayName("updateRate should reject null or non-positive values")
    void updateRateRejectsInvalidRates() {
        RateConfigService service = new RateConfigService("PERSONAL=12");

        assertThrows(IllegalArgumentException.class, () -> service.updateRate("PERSONAL", null));
        assertThrows(IllegalArgumentException.class, () -> service.updateRate("PERSONAL", 0.0));
        assertThrows(IllegalArgumentException.class, () -> service.updateRate("PERSONAL", -1.0));
    }

    @Test
    @DisplayName("getRate falls back to defaults when not overridden")
    void getRateUsesDefaultsWhenNoOverride() {
        RateConfigService service = new RateConfigService("AUTO=10");

        assertEquals(10.0, service.getRate("auto"));
    }

    @Test
    @DisplayName("getRate falls back to 12 when type missing")
    void getRateFallsBackToTwelveWhenUnknown() {
        RateConfigService service = new RateConfigService("PERSONAL=10");

        assertEquals(12.0, service.getRate("BOAT"));
    }

    @Test
    @DisplayName("resetToDefaults restores initial rates")
    void resetToDefaultsRestoresDefaults() {
        RateConfigService service = new RateConfigService("PERSONAL=12");
        service.updateRate("PERSONAL", 20.0);

        service.resetToDefaults();

        assertEquals(12.0, service.getRate("PERSONAL"));
    }

    @Test
    @DisplayName("Invalid numbers in config should be ignored without failing")
    void invalidConfigValuesAreIgnored() {
        RateConfigService service = new RateConfigService("PERSONAL=abc,AUTO=10");

        Map<String, Double> rates = service.getAllRates();

        assertFalse(rates.containsKey("PERSONAL"));
        assertEquals(10.0, rates.get("AUTO"));
    }
}
