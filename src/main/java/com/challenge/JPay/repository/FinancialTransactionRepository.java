package com.challenge.JPay.repository;

import com.challenge.JPay.model.FinancialTransaction;
import com.challenge.JPay.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    Page<FinancialTransaction> findByBankAccountId(Long bankAccountId, Pageable pageable);

    Page<FinancialTransaction> findByType(TransactionType type, Pageable pageable);

    @Query("SELECT t FROM FinancialTransaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    Page<FinancialTransaction> findByTransactionDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<FinancialTransaction> findByAccountPayableId(Long accountId);
}
