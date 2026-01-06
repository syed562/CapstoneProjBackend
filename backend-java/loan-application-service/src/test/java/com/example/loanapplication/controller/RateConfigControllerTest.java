package com.example.loanapplication.controller;

import com.example.loanapplication.controller.dto.UpdateRateRequest;
import com.example.loanapplication.service.RateConfigService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateConfigControllerTest {

    @Mock
    private RateConfigService rateConfigService;

    @InjectMocks
    private RateConfigController rateConfigController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return all rates for authenticated user")
    void getAllRates_success() {
        Map<String, Double> rates = Map.of("PERSONAL", 12.0, "AUTO", 10.0);
        when(rateConfigService.getAllRates()).thenReturn(rates);

        ResponseEntity<Map<String, Double>> response = rateConfigController.getAllRates();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rates, response.getBody());
        verify(rateConfigService).getAllRates();
    }

    @Test
    @DisplayName("Admin can update rate")
    void updateRate_adminSuccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        UpdateRateRequest req = new UpdateRateRequest();
        req.setLoanType("auto");
        req.setRate(9.5);

        ResponseEntity<Map<String, Object>> response = rateConfigController.updateRate(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AUTO", response.getBody().get("loanType"));
        assertEquals(9.5, response.getBody().get("rate"));
        verify(rateConfigService).updateRate("AUTO", 9.5);
    }

    @Test
    @DisplayName("Non-admin cannot reset rates")
    void resetRates_nonAdminForbidden() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
        );

        assertThrows(ResponseStatusException.class, () -> rateConfigController.resetRates());
        verify(rateConfigService, never()).resetToDefaults();
    }

    @Test
    @DisplayName("Admin can reset rates")
    void resetRates_adminSuccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        ResponseEntity<Map<String, Object>> response = rateConfigController.resetRates();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(rateConfigService).resetToDefaults();
        verify(rateConfigService).getAllRates();
    }
}
