package com.example.loanservice.repayment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, String> {
    List<Repayment> findByLoanId(String loanId);
    List<Repayment> findByEmiScheduleId(String emiScheduleId);
    List<Repayment> findByStatus(String status);
}
