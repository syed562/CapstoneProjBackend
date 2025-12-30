package com.example.loanservice.emi;

import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EMIService {
    private final EMIScheduleRepository emiRepository;
    private final LoanRepository loanRepository;

    public EMIService(EMIScheduleRepository emiRepository, LoanRepository loanRepository) {
        this.emiRepository = emiRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * Generate EMI schedule for approved loan
     */
    public List<EMISchedule> generateEMISchedule(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        if (loan.getRatePercent() == null || loan.getRatePercent() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan must have a valid interest rate");
        }

        // Delete existing schedule if present
        List<EMISchedule> existing = emiRepository.findByLoanId(loanId);
        if (!existing.isEmpty()) {
            emiRepository.deleteAll(existing);
        }

        double emiAmount = EMICalculator.calculateEMI(loan.getAmount(), loan.getRatePercent(), loan.getTermMonths());
        List<EMISchedule> schedules = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();
        double outstandingBalance = loan.getAmount();

        for (int month = 1; month <= loan.getTermMonths(); month++) {
            double monthlyRate = loan.getRatePercent() / 12 / 100;
            double interestAmount = outstandingBalance * monthlyRate;
            double principalAmount = emiAmount - interestAmount;
            outstandingBalance -= principalAmount;

            EMISchedule schedule = new EMISchedule();
            schedule.setId(UUID.randomUUID().toString());
            schedule.setLoanId(loanId);
            schedule.setMonth(month);
            schedule.setEmiAmount(Math.round(emiAmount * 100.0) / 100.0);
            schedule.setPrincipalAmount(Math.round(principalAmount * 100.0) / 100.0);
            schedule.setInterestAmount(Math.round(interestAmount * 100.0) / 100.0);
            schedule.setOutstandingBalance(Math.round(Math.max(0, outstandingBalance) * 100.0) / 100.0);
            schedule.setStatus("SCHEDULED");
            schedule.setDueDate(currentMonth.plusMonths(month).atEndOfMonth().toString());
            schedule.setCreatedAt(java.time.Instant.now().toString());

            schedules.add(schedule);
        }

        return emiRepository.saveAll(schedules);
    }

    public List<EMISchedule> getEMISchedule(String loanId) {
        return emiRepository.findByLoanIdOrderByMonth(loanId);
    }

    public EMISchedule getEMIScheduleItem(String scheduleId) {
        return emiRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EMI schedule not found"));
    }

    public EMISchedule markAsPaid(String scheduleId) {
        EMISchedule schedule = getEMIScheduleItem(scheduleId);
        schedule.setStatus("PAID");
        schedule.setPaidDate(LocalDate.now().toString());
        return emiRepository.save(schedule);
    }
}
