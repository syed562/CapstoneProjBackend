package com.example.loanservice.client;

import com.example.loanservice.client.dto.LoanApplicationView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-application-service", url = "${loan-application-service.url}", fallback = LoanApplicationClientFallback.class)
public interface LoanApplicationClient {

    @GetMapping("/api/loan-applications/{applicationId}")
    LoanApplicationView getApplication(@PathVariable("applicationId") String applicationId);
}
