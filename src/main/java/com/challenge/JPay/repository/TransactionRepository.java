package com.challenge.JPay.repository;

import com.challenge.JPay.model.Transaction;
import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByStatus(Status status, Pageable pageable);

    @Query("SELECT a FROM Transaction a WHERE a.expirationDate < :currentDate AND a.status = 'PENDING'")
    Page<Transaction> findByExpiredTransactions(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    Page<Transaction> findByExpirationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT a FROM Transaction a WHERE a.expirationDate BETWEEN :startDate AND :endDate AND a.transactionType = :type")
    Page<Transaction> findByExpirationDateBetweenAndType(LocalDate startDate, LocalDate endDate, TransactionType type, Pageable pageable);

    @Query("SELECT SUM(a.amount) FROM Transaction a WHERE a.transactionType = :type")
    BigDecimal getTotalAmountByType(TransactionType type);
}
