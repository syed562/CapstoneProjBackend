package com.example.loanservice.repayment;

import com.example.loanservice.emi.EMISchedule;
import com.example.loanservice.emi.EMIService;
import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RepaymentService {
    private final RepaymentRepository repaymentRepository;
    private final EMIService emiService;
    private final LoanRepository loanRepository;

    public RepaymentService(
            RepaymentRepository repaymentRepository,
            EMIService emiService,
            LoanRepository loanRepository
    ) {
        this.repaymentRepository = repaymentRepository;
        this.emiService = emiService;
        this.loanRepository = loanRepository;
    }

    public Repayment recordPayment(
            String loanId,
            String emiScheduleId,
            double amountPaid,
            String paymentMethod,
            String transactionId
    ) {
        // Validate loan and EMI exist
        emiService.getEMIScheduleItem(emiScheduleId);
        loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        Repayment repayment = new Repayment();
        repayment.setId(UUID.randomUUID().toString());
        repayment.setLoanId(loanId);
        repayment.setEmiScheduleId(emiScheduleId);
        repayment.setAmountPaid(amountPaid);
        repayment.setPaymentDate(java.time.LocalDate.now().toString());
        repayment.setPaymentMethod(paymentMethod);
        repayment.setTransactionId(transactionId);
        repayment.setStatus("COMPLETED");
        repayment.setCreatedAt(Instant.now().toString());

        // Mark EMI as paid
        emiService.markAsPaid(emiScheduleId);

        return repaymentRepository.save(repayment);
    }

    public List<Repayment> getRepaymentsByLoan(String loanId) {
        return repaymentRepository.findByLoanId(loanId);
    }

    public List<Repayment> getRepaymentsByEMI(String emiScheduleId) {
        return repaymentRepository.findByEmiScheduleId(emiScheduleId);
    }

    public double getOutstandingBalance(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        List<EMISchedule> schedules = emiService.getEMISchedule(loanId);
        return schedules.stream()
                .filter(s -> "SCHEDULED".equals(s.getStatus()))
                .mapToDouble(EMISchedule::getOutstandingBalance)
                .max()
                .orElse(0.0);
    }

    public long getCompletedPayments(String loanId) {
        return repaymentRepository.findByLoanId(loanId)
                .stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .count();
    }
}
