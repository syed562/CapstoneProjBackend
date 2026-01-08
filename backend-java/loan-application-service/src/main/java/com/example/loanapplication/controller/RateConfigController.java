package com.example.loanapplication.controller;

import com.example.loanapplication.controller.dto.UpdateRateRequest;
import com.example.loanapplication.service.RateConfigService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rates")
public class RateConfigController {
    private final RateConfigService rateConfigService;

    public RateConfigController(RateConfigService rateConfigService) {
        this.rateConfigService = rateConfigService;
    }

  
    @GetMapping
    public ResponseEntity<Map<String, Double>> getAllRates() {
        return ResponseEntity.ok(rateConfigService.getAllRates());
    }

   
    @PostMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRate(@Valid @RequestBody UpdateRateRequest request) {
        String loanType = request.getLoanType().toUpperCase();
        Double rate = request.getRate();

        log.info("[RATE-CONFIG] Admin updating rate for loan type: {} to {}", loanType, rate);

        rateConfigService.updateRate(loanType, rate);

        return ResponseEntity.ok(Map.of(
            "message", "Rate updated successfully",
            "loanType", loanType,
            "rate", rate
        ));
    }

    
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetRates() {
        requireAdmin();

        log.info("[RATE-CONFIG] Admin resetting all rates to defaults");
        rateConfigService.resetToDefaults();

        return ResponseEntity.ok(Map.of(
            "message", "All rates reset to defaults",
            "rates", rateConfigService.getAllRates()
        ));
    }

   
    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}
