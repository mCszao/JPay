package com.challenge.JPay.repository;

import com.challenge.JPay.model.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByActiveTrue();

    Page<BankAccount> findByBankContainingIgnoreCase(String bank, Pageable pageable);

    @Query("SELECT SUM(ba.currentBalance) FROM BankAccount ba WHERE ba.active = true")
    Optional<BigDecimal> sumCurrentBalanceByActiveTrue();

    @Query("SELECT COUNT(a) FROM AccountPayable a WHERE a.bankAccount.id = :bankAccountId AND a.status = 'PENDING'")
    long countPendingAccountsByBankAccount(@Param("bankAccountId") Long bankAccountId);

    @Query("SELECT COUNT(a) FROM AccountPayable a WHERE a.bankAccount.id = :bankAccountId")
    long countAccountsByBankAccount(@Param("bankAccountId") Long bankAccountId);
}
