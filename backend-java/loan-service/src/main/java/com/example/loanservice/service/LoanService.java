package com.example.loanservice.service;

import com.example.loanservice.client.LoanApplicationClient;
import com.example.loanservice.client.dto.LoanApplicationView;
import com.example.loanservice.domain.Loan;
import com.example.loanservice.domain.LoanRepository;
import com.example.loanservice.domain.LoanType;
import com.example.loanservice.emi.EMIService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {
    private final LoanRepository repo;
    private final LoanApplicationClient loanApplicationClient;
    private final EMIService emiService;
    private final LoanNotificationService notificationService;

    public LoanService(LoanRepository repo, LoanApplicationClient loanApplicationClient, 
                     EMIService emiService, LoanNotificationService notificationService) {
        this.repo = repo;
        this.loanApplicationClient = loanApplicationClient;
        this.emiService = emiService;
        this.notificationService = notificationService;
    }

    public List<Loan> list() {
        return repo.findAll();
    }

    public List<Loan> listByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return repo.findByUserId(userId);
    }

    public Loan get(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    public Loan create(String userId, LoanType loanType, double amount, int termMonths, Double ratePercent) {
        String now = Instant.now().toString();
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserId(userId);
        loan.setAmount(amount);
        loan.setLoanType(loanType);
        loan.setTermMonths(termMonths);
        loan.setRatePercent(ratePercent);
        loan.setStatus("pending");
        loan.setOutstandingBalance(amount);  // Initialize with full loan amount
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
        return repo.save(loan);
    }

    public Loan approveFromApplication(String applicationId) {
        LoanApplicationView app = loanApplicationClient.getApplication(applicationId);
        if (app == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }
        String now = Instant.now().toString();
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserId(app.getUserId());
        loan.setAmount(app.getAmount());
        try {
            loan.setLoanType(LoanType.valueOf(app.getLoanType().toUpperCase()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid loan type on application: " + app.getLoanType());
        }
        loan.setTermMonths(app.getTermMonths());
        loan.setRatePercent(app.getRatePercent());
        loan.setStatus("approved");
        loan.setOutstandingBalance(app.getAmount());  // Initialize with full amount
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
        Loan saved = repo.save(loan);

        // Auto-generate EMI schedule after approval
        emiService.generateEMISchedule(saved.getId());
        
        // Send EMI notification to customer
        if (app.getRatePercent() != null) {
            notificationService.sendEMINotification(app.getUserId(), saved.getId(), 
                app.getAmount(), app.getRatePercent(), app.getTermMonths());
        }
        
        return saved;
    }

    public Loan updateStatus(String id, String status) {
        Loan loan = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        loan.setStatus(status);
        loan.setUpdatedAt(Instant.now().toString());
        return repo.save(loan);
    }

    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found");
        }
        repo.deleteById(id);
    }
}
