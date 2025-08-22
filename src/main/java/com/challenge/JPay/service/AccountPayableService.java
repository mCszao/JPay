package com.challenge.JPay.service;

import com.challenge.JPay.dto.request.AccountPayableRequestDTO;
import com.challenge.JPay.dto.request.PaymentRequestDTO;
import com.challenge.JPay.dto.response.AccountPayableResponseDTO;
import com.challenge.JPay.dto.response.BankAccountResponseDTO;
import com.challenge.JPay.dto.response.CategoryResponseDTO;
import com.challenge.JPay.exception.*;
import com.challenge.JPay.model.AccountPayable;
import com.challenge.JPay.model.BankAccount;
import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.model.enums.TransactionType;
import com.challenge.JPay.repository.AccountPayableRepository;
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
public class AccountPayableService {

    private final AccountPayableRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;

    public Page<AccountPayableResponseDTO> findAll(Pageable pageable) {
        log.info("Finding all accounts with pagination: {}", pageable);

        return accountRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public AccountPayableResponseDTO findById(Long id) {
        log.info("Finding account by id: {}", id);

        var account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountPayableNotFoundException(id));
        return toResponseDTO(account);
    }

    public Page<AccountPayableResponseDTO> findByStatus(Status status, Pageable pageable) {
        log.info("Finding accounts by status: {} with pagination: {}", status, pageable);

        return accountRepository.findByStatus(status, pageable)
                .map(this::toResponseDTO);
    }

    public Page<AccountPayableResponseDTO> findOverdueAccounts(Pageable pageable) {
        log.info("Finding overdue accounts with pagination: {}", pageable);

        return accountRepository.findOverdueAccounts(LocalDate.now(), pageable)
                .map(this::toResponseDTO);
    }

    public Page<AccountPayableResponseDTO> findByExpirationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Finding accounts by expiration date date between {} and {} with pagination: {}", startDate, endDate, pageable);

        return accountRepository.findByExpirationDateBetween(startDate, endDate, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public AccountPayableResponseDTO create(AccountPayableRequestDTO dto) {
        log.info("Creating new account: {}", dto.description());

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

        var account = AccountPayable.builder()
                .description(dto.description())
                .amount(dto.amount())
                .expirationDate(dto.expirationDate())
                .transactionType(Enum.valueOf(TransactionType.class,dto.type()))
                .status(Status.PENDING)
                .category(category)
                .bankAccount(bankAccount)
                .build();

        var createdAccount = accountRepository.save(account);
        log.info("Account created successfully with id: {}", createdAccount.getId());

        return toResponseDTO(createdAccount);
    }

    @Transactional
    public AccountPayableResponseDTO update(Long id, AccountPayableRequestDTO dto) {
        log.info("Updating account with id: {}", id);

        var account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountPayableNotFoundException(id));

        if (account.getStatus() == Status.PAID) {
            throw new BusinessException("Não é possível modificar uma conta depois de paga");
        }

        var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.categoryId()));

        BankAccount bankAccount = bankAccountRepository.findById(dto.bankAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException(dto.bankAccountId()));

        account.setDescription(dto.description());
        account.setAmount(dto.amount());
        account.setExpirationDate(dto.expirationDate());
        account.setCategory(category);
        account.setBankAccount(bankAccount);

        var updatedAccount = accountRepository.save(account);
        log.info("Account updated successfully with id: {}", updatedAccount.getId());

        return toResponseDTO(updatedAccount);
    }

    @Transactional
    public AccountPayableResponseDTO payAccount(PaymentRequestDTO dto) {
        log.info("Processing payment for account id: {}", dto.accountId());

        var account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new AccountPayableNotFoundException(dto.accountId()));

        BankAccount bankAccount = bankAccountRepository.findById(dto.bankAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException(dto.bankAccountId()));

        BigDecimal currentBalance = bankAccount.getCurrentBalance();
        BigDecimal newBalance = currentBalance.subtract(account.getAmount());
        account.markAsPaid();

        bankAccount.setCurrentBalance(newBalance);
        bankAccountRepository.save(bankAccount);
        var paidAccount = accountRepository.save(account);
        log.info("Account paid successfully with id: {}", paidAccount.getId());

        return toResponseDTO(paidAccount);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting account with id: {}", id);

        var account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        accountRepository.delete(account);
        log.info("Account deleted successfully with id: {}", id);
    }

    private AccountPayableResponseDTO toResponseDTO(AccountPayable account) {
        return AccountPayableResponseDTO.builder()
                .id(account.getId())
                .description(account.getDescription())
                .amount(account.getAmount())
                .expirationDate(account.getExpirationDate())
                .paymentDate(account.getPaymentDate())
                .status(account.getStatus())
                .type(account.getTransactionType())
                .isExpired(account.isExpired())
                .category(CategoryResponseDTO.builder()
                        .id(account.getCategory().getId())
                        .name(account.getCategory().getName())
                        .build())
                .bankAccount(BankAccountResponseDTO.builder()
                        .id(account.getBankAccount().getId())
                        .name(account.getBankAccount().getName())
                        .bank(account.getBankAccount().getBank())
                        .build())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}