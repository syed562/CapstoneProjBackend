package com.example.reportservice.client;

import com.example.reportservice.client.dto.LoanApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "loan-application-service", url = "${loan-application-service.url}", fallback = LoanApplicationClientFallback.class)
public interface LoanApplicationClient {
    @GetMapping("/api/loan-applications")
    List<LoanApplicationDTO> getAllApplications();
}
