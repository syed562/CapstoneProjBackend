package com.example.loanservice.repository;

import com.example.loanservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByLoanIdOrderByCreatedAtDesc(String loanId);
    List<Payment> findByEmiId(String emiId);
}
