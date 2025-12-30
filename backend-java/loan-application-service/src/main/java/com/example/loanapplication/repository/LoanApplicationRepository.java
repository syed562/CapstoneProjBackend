package com.example.loanapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.loanapplication.MODELS.LoanApplication;
import com.example.loanapplication.MODELS.LoanType;
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {
    List<LoanApplication> findByUserId(String userId);
    List<LoanApplication> findByUserIdAndLoanTypeAndStatusIn(String userId, LoanType loanType, List<String> statuses);
}
