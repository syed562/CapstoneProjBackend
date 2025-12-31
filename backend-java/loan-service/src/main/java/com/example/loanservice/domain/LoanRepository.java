package com.example.loanservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, String> {
    List<Loan> findByUserId(String userId);

    // JPQL example: filter by status and amount range with ordering
    @Query("select l from Loan l where (:status is null or lower(l.status) = lower(:status)) " +
	    "and (:minAmount is null or l.amount >= :minAmount) " +
	    "and (:maxAmount is null or l.amount <= :maxAmount) " +
	    "order by l.createdAt desc")
    List<Loan> findByStatusAndAmountRange(
	     @Param("status") String status,
	     @Param("minAmount") Double minAmount,
	     @Param("maxAmount") Double maxAmount);
}
