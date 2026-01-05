package com.example.reportservice.client;

import com.example.reportservice.client.dto.LoanDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "loan-service", fallback = LoanClientFallback.class)
public interface LoanClient {
    @GetMapping("/api/loans")
    List<LoanDTO> getAllLoans();
}
