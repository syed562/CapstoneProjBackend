package com.example.loanservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, String> {
	List<Loan> findByUserId(String userId);
}
