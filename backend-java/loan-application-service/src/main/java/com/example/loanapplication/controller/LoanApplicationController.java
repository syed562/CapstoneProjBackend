package com.example.loanapplication.controller;

import com.example.loanapplication.DTO.ApplyRequest;
import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
@CrossOrigin
public class LoanApplicationController {
    private final LoanApplicationService apps;

    public LoanApplicationController(LoanApplicationService apps) {
        this.apps = apps;
    }

    @PostMapping("/apply")
    public LoanApplication apply(@Valid @RequestBody ApplyRequest req) {
        return apps.apply(req.getUserId(), req.getAmount(), req.getTermMonths(), req.getRatePercent());
    }

    @GetMapping("/my")
    public List<LoanApplication> my(@RequestParam("userId") String userId) {
        return apps.listByUser(userId);
    }

    @GetMapping("/{applicationId}")
    public LoanApplication get(@PathVariable("applicationId") String applicationId) {
        return apps.get(applicationId);
    }

    @PutMapping("/{applicationId}/review")
    public LoanApplication markUnderReview(@PathVariable("applicationId") String applicationId) {
        return apps.markUnderReview(applicationId);
    }
}
