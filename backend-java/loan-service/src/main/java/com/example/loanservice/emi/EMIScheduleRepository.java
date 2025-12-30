package com.example.loanservice.emi;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EMIScheduleRepository extends JpaRepository<EMISchedule, String> {
    List<EMISchedule> findByLoanId(String loanId);
    List<EMISchedule> findByLoanIdOrderByMonth(String loanId);
    List<EMISchedule> findByStatus(String status);
}
