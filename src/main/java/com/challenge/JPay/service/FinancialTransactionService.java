package com.challenge.JPay.service;

import com.challenge.JPay.dto.request.PaymentRequestDTO;
import com.challenge.JPay.dto.response.FinancialTransactionResponseDTO;
import com.challenge.JPay.exception.BankAccountNotFoundException;
import com.challenge.JPay.exception.FinancialTransactionNotFoundException;
import com.challenge.JPay.model.AccountPayable;
import com.challenge.JPay.model.BankAccount;
import com.challenge.JPay.model.FinancialTransaction;
import com.challenge.JPay.model.enums.TransactionType;
import com.challenge.JPay.repository.BankAccountRepository;
import com.challenge.JPay.repository.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public Page<FinancialTransactionResponseDTO> findAll(Pageable pageable) {
        log.info("Finding all financial transactions with pagination: {}", pageable);

        return transactionRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public FinancialTransactionResponseDTO findById(Long id) {
        log.info("Finding financial transaction by id: {}", id);

        FinancialTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new FinancialTransactionNotFoundException(id));
        return toResponseDTO(transaction);
    }

    public Page<FinancialTransactionResponseDTO> findByBankAccountId(Long bankAccountId, Pageable pageable) {
        log.info("Finding financial transactions by bank account id: {} with pagination: {}", bankAccountId, pageable);

        return transactionRepository.findByBankAccountId(bankAccountId, pageable)
                .map(this::toResponseDTO);
    }

    public Page<FinancialTransactionResponseDTO> findByType(TransactionType type, Pageable pageable) {
        log.info("Finding financial transactions by type: {} with pagination: {}", type, pageable);

        return transactionRepository.findByType(type, pageable)
                .map(this::toResponseDTO);
    }

    public Page<FinancialTransactionResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Finding financial transactions between {} and {} with pagination: {}", startDate, endDate, pageable);

        return transactionRepository.findByTransactionDateBetween(startDate, endDate, pageable)
                .map(this::toResponseDTO);
    }

    public List<FinancialTransactionResponseDTO> findByAccountId(Long accountId) {
        log.info("Finding financial transactions by account id: {}", accountId);

        return transactionRepository.findByAccountPayableId(accountId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public FinancialTransactionResponseDTO processPayment(AccountPayable account, BankAccount bankAccount, PaymentRequestDTO payment) {
        log.info("Processing payment for account id: {} from bank account id: {}", account.getId(), bankAccount.getId());

        BigDecimal previousBalance = bankAccount.getCurrentBalance();
        BigDecimal newBalance = previousBalance.subtract(account.getAmount());

        FinancialTransaction transaction = FinancialTransaction.builder()
                .bankAccount(bankAccount)
                .accountPayable(account)
                .type(payment.type())
                .amount(account.getAmount())
                .description("Payment for: " + account.getDescription())
                .previousBalance(previousBalance)
                .currentBalance(newBalance)
                .build();

        FinancialTransaction savedTransaction = transactionRepository.save(transaction);

        bankAccount.setCurrentBalance(newBalance);
        bankAccountRepository.save(bankAccount);

        log.info("Payment processed successfully. Transaction id: {}", savedTransaction.getId());
        return toResponseDTO(savedTransaction);
    }

//    @Transactional
//    public FinancialTransactionResponseDTO createCreditTransaction(Long bankAccountId, BigDecimal amount, String description) {
//        log.info("Creating credit transaction for bank account id: {} with amount: {}", bankAccountId, amount);
//
//        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
//                .orElseThrow(() -> new BankAccountNotFoundException(bankAccountId));
//
//        BigDecimal previousBalance = bankAccount.getCurrentBalance();
//        BigDecimal newBalance = previousBalance.add(amount);
//
//        FinancialTransaction transaction = FinancialTransaction.builder()
//                .bankAccount(bankAccount)
//                .type(TransactionType.ATIVO)
//                .amount(amount)
//                .description(description)
//                .previousBalance(previousBalance)
//                .currentBalance(newBalance)
//                .build();
//
//        FinancialTransaction savedTransaction = transactionRepository.save(transaction);
//
//        // Atualizar saldo da conta bancária
//        bankAccount.setCurrentBalance(newBalance);
//        bankAccountRepository.save(bankAccount);
//
//        log.info("Credit transaction created successfully. Transaction id: {}", savedTransaction.getId());
//        return toResponseDTO(savedTransaction);
//    }
//
//    @Transactional
//    public FinancialTransactionResponseDTO createDebitTransaction(Long bankAccountId, BigDecimal amount, String description) {
//        log.info("Creating debit transaction for bank account id: {} with amount: {}", bankAccountId, amount);
//
//        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
//                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));
//
//        BigDecimal previousBalance = bankAccount.getCurrentBalance();
//        BigDecimal newBalance = previousBalance.subtract(amount);
//
//        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
//            throw new BusinessException("Insufficient balance for this transaction");
//        }
//
//        FinancialTransaction transaction = FinancialTransaction.builder()
//                .bankAccount(bankAccount)
//                .type(TransactionType.PASSIVO)
//                .amount(amount)
//                .description(description)
//                .previousBalance(previousBalance)
//                .currentBalance(newBalance)
//                .build();
//
//        FinancialTransaction savedTransaction = transactionRepository.save(transaction);
//
//        // Atualizar saldo da conta bancária
//        bankAccount.setCurrentBalance(newBalance);
//        bankAccountRepository.save(bankAccount);
//
//        log.info("Debit transaction created successfully. Transaction id: {}", savedTransaction.getId());
//        return toResponseDTO(savedTransaction);
//    }

    public void delete(FinancialTransaction transaction) {
        transactionRepository.delete(transaction);
    }

    private FinancialTransactionResponseDTO toResponseDTO(FinancialTransaction transaction) {
        return FinancialTransactionResponseDTO.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .previousBalance(transaction.getPreviousBalance())
                .currentBalance(transaction.getCurrentBalance())
                .transactionDate(transaction.getTransactionDate())
                .bankAccountId(transaction.getBankAccount().getId())
                .bankAccountName(transaction.getBankAccount().getName())
                .accountId(transaction.getAccountPayable() != null ? transaction.getAccountPayable().getId() : null)
                .accountDescription(transaction.getAccountPayable() != null ? transaction.getAccountPayable().getDescription() : null)
                .build();
    }
}