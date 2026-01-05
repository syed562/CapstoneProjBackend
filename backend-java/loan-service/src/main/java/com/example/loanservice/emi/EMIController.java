package com.example.loanservice.emi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans/{loanId}/emi")
public class EMIController {
    private final EMIService emiService;

    public EMIController(EMIService emiService) {
        this.emiService = emiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<EMISchedule>> generateSchedule(@PathVariable String loanId) {
        return ResponseEntity.ok(emiService.generateEMISchedule(loanId));
    }

    @GetMapping
    public ResponseEntity<List<EMISchedule>> getSchedule(@PathVariable String loanId) {
        return ResponseEntity.ok(emiService.getEMISchedule(loanId));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<EMISchedule> getScheduleItem(
            @PathVariable String loanId,
            @PathVariable String scheduleId
    ) {
        return ResponseEntity.ok(emiService.getEMIScheduleItem(scheduleId));
    }
}
