package com.challenge.JPay.service;

import com.challenge.JPay.dto.request.TransactionRequestDTO;
import com.challenge.JPay.dto.request.PaymentRequestDTO;
import com.challenge.JPay.dto.response.TransactionResponseDTO;
import com.challenge.JPay.dto.response.BankAccountResponseDTO;
import com.challenge.JPay.dto.response.CategoryResponseDTO;
import com.challenge.JPay.exception.*;
import com.challenge.JPay.model.Transaction;
import com.challenge.JPay.model.BankAccount;
import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.model.enums.TransactionType;
import com.challenge.JPay.repository.TransactionRepository;
import com.challenge.JPay.repository.BankAccountRepository;
import com.challenge.JPay.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;

    public Page<TransactionResponseDTO> findAll(Pageable pageable) {
        log.info("Finding all transactions with pagination: {}", pageable);

        return transactionRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public TransactionResponseDTO findById(Long id) {
        log.info("Finding transaction by id: {}", id);

        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return toResponseDTO(transaction);
    }

    public Page<TransactionResponseDTO> findByStatus(Status status, Pageable pageable) {
        log.info("Finding transactions by status: {} with pagination: {}", status, pageable);

        return transactionRepository.findByStatus(status, pageable)
                .map(this::toResponseDTO);
    }

    public Page<TransactionResponseDTO> findByExpiredTransactions(Pageable pageable) {
        log.info("Finding overdue transactions with pagination: {}", pageable);

        return transactionRepository.findByExpiredTransactions(LocalDate.now(), pageable)
                .map(this::toResponseDTO);
    }

    public Page<TransactionResponseDTO> findByExpirationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Finding transactions by expiration date date between {} and {} with pagination: {}", startDate, endDate, pageable);

        return transactionRepository.findByExpirationDateBetween(startDate, endDate, pageable)
                .map(this::toResponseDTO);
    }

    public Page<TransactionResponseDTO> findByExpirationDateBetweenAndType(LocalDate startDate, LocalDate endDate, String type, Pageable pageable) {
        log.info("Finding transactions by expiration date date between {} and {} and type {} with pagination: {}", startDate, endDate, type, pageable);

        return transactionRepository.findByExpirationDateBetweenAndType(startDate, endDate, Enum.valueOf(TransactionType.class, type), pageable).map(this::toResponseDTO);
    }

    public BigDecimal getTotalAmountByType(String type) {
        log.info("Calculating total amount by type: {}", type);

        return transactionRepository.getTotalAmountByType(Enum.valueOf(TransactionType.class, type));
    }

    @Transactional
    public TransactionResponseDTO create(TransactionRequestDTO dto) {
        log.info("Creating new transaction: {}", dto.description());

        var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.categoryId()));

        BankAccount bankAccount = bankAccountRepository.findById(dto.bankAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException(dto.bankAccountId()));

        if (!category.getActive()) {
            throw new BusinessException("Não pode criar uma conta a pagar com uma categoria inativa");
        }

        if (!bankAccount.getActive()) {
            throw new BusinessException("Não pode criar uma conta a pagar com uma conta bancária inativa");
        }

        var transaction = Transaction.builder()
                .description(dto.description())
                .amount(dto.amount())
                .expirationDate(dto.expirationDate())
                .transactionType(Enum.valueOf(TransactionType.class,dto.type()))
                .status(dto.status() == null ? Status.PENDING : Enum.valueOf(Status.class, dto.status()))
                .category(category)
                .bankAccount(bankAccount)
                .build();

        var createdTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with id: {}", createdTransaction.getId());

        return toResponseDTO(createdTransaction);
    }

    @Transactional
    public TransactionResponseDTO update(Long id, TransactionRequestDTO dto) {
        log.info("Updating transaction with id: {}", id);

        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        if (transaction.getStatus() == Status.PAID) {
            throw new BusinessException("Não é possível modificar uma conta depois de paga");
        }

        var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.categoryId()));

        BankAccount bankAccount = bankAccountRepository.findById(dto.bankAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException(dto.bankAccountId()));

        transaction.setDescription(dto.description());
        transaction.setAmount(dto.amount());
        transaction.setTransactionType(Enum.valueOf(TransactionType.class, dto.type()));
        transaction.setStatus(Enum.valueOf(Status.class, dto.status()));
        transaction.setExpirationDate(dto.expirationDate());
        transaction.setCategory(category);
        transaction.setBankAccount(bankAccount);

        var updatedTransaction = transactionRepository.save(transaction);
        log.info("transaction updated successfully with id: {}", updatedTransaction.getId());

        return toResponseDTO(updatedTransaction);
    }

    @Transactional
    public TransactionResponseDTO payTransaction(PaymentRequestDTO dto) {
        log.info("Processing payment for transaction id: {}", dto.transactionId());

        var transaction = transactionRepository.findById(dto.transactionId())
                .orElseThrow(() -> new TransactionNotFoundException(dto.transactionId()));

        BankAccount bankAccount = bankAccountRepository.findById(dto.bankAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException(dto.bankAccountId()));

        BigDecimal currentBalance = bankAccount.getCurrentBalance();
        BigDecimal newBalance = currentBalance.subtract(transaction.getAmount());
        transaction.markAsPaid();

        bankAccount.setCurrentBalance(newBalance);
        bankAccountRepository.save(bankAccount);
        var paidTransaction = transactionRepository.save(transaction);
        log.info("Transaction paid successfully with id: {}", paidTransaction.getId());

        return toResponseDTO(paidTransaction);
    }

    @Transactional
    public TransactionResponseDTO payTransaction(Long id) {
        log.info("Processing payment for transaction id: {}",id);

        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        var bankAccount = bankAccountRepository.findById(transaction.getBankAccount().getId())
                .orElseThrow(() -> new BankAccountNotFoundException(id));
        BigDecimal currentBalance = bankAccount.getCurrentBalance();
        BigDecimal newBalance = currentBalance.subtract(transaction.getAmount());
        transaction.markAsPaid();

        bankAccount.setCurrentBalance(newBalance);
        bankAccountRepository.save(bankAccount);
        var paidTransaction = transactionRepository.save(transaction);
        log.info("Transaction paid successfully with id: {}", paidTransaction.getId());

        return toResponseDTO(paidTransaction);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting transaction with id: {}", id);

        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully with id: {}", id);
    }

    private TransactionResponseDTO toResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .expirationDate(transaction.getExpirationDate())
                .paymentDate(transaction.getPaymentDate())
                .status(transaction.getStatus())
                .type(transaction.getTransactionType())
                .isExpired(transaction.isExpired())
                .category(CategoryResponseDTO.builder()
                        .id(transaction.getCategory().getId())
                        .name(transaction.getCategory().getName())
                        .build())
                .bankAccount(BankAccountResponseDTO.builder()
                        .id(transaction.getBankAccount().getId())
                        .name(transaction.getBankAccount().getName())
                        .bank(transaction.getBankAccount().getBank())
                        .build())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}