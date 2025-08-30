package com.challenge.JPay.service;

import com.challenge.JPay.dto.request.BankAccountRequestDTO;
import com.challenge.JPay.dto.response.BankAccountResponseDTO;
import com.challenge.JPay.exception.BankAccountNotFoundException;
import com.challenge.JPay.exception.BusinessException;
import com.challenge.JPay.model.BankAccount;
import com.challenge.JPay.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public Page<BankAccountResponseDTO> findAll(Pageable pageable) {
        log.info("Finding all bank accounts with pagination: {}", pageable);

        return bankAccountRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public List<BankAccountResponseDTO> findAllActive() {
        log.info("Finding all active bank accounts");

        return bankAccountRepository.findByActiveTrue()
                .stream().map(this::toResponseDTO).toList();
    }

    public BankAccountResponseDTO findById(Long id) {
        log.info("Finding bank account by id: {}", id);

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));
        return toResponseDTO(bankAccount);
    }

    public Page<BankAccountResponseDTO> findByBank(String bank, Pageable pageable) {
        log.info("Finding bank accounts by bank: {} with pagination: {}", bank, pageable);

        return bankAccountRepository.findByBankContainingIgnoreCase(bank, pageable)
                .map(this::toResponseDTO);
    }

    public BigDecimal getTotalBalance() {
        log.info("Calculating total balance across all active bank accounts");

        return bankAccountRepository.sumCurrentBalanceByActiveTrue()
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public BankAccountResponseDTO create(BankAccountRequestDTO dto) {
        log.info("Creating new bank account: {}", dto.name());

        BankAccount bankAccount = BankAccount.builder()
                .name(dto.name())
                .bank(dto.bank())
                .currentBalance(dto.currentBalance())
                .build();

        BankAccount createdBankAccount = bankAccountRepository.save(bankAccount);
        log.info("Bank account created successfully with id: {}", createdBankAccount.getId());

        return toResponseDTO(createdBankAccount);
    }

    @Transactional
    public BankAccountResponseDTO update(Long id, BankAccountRequestDTO dto) {
        log.info("Updating bank account with id: {}", id);

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        bankAccount.setName(dto.name());
        bankAccount.setBank(dto.bank());
        bankAccount.setCurrentBalance(dto.currentBalance());

        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        log.info("Bank account updated successfully with id: {}", updatedBankAccount.getId());

        return toResponseDTO(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDTO updateBalance(Long id, BigDecimal newBalance) {
        log.info("Updating balance for bank account id: {} to: {}", id, newBalance);

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Balance não pode ser menor que 0");
        }

        bankAccount.setCurrentBalance(newBalance);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        log.info("Bank account balance updated successfully for id: {}", updatedBankAccount.getId());

        return toResponseDTO(updatedBankAccount);
    }

    @Transactional
    public void deactivate(Long id) {
        log.info("Deactivating/activating bank account with id: {}", id);

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        long pendingTransactionsCount = bankAccountRepository.countPendingTransactionsByBankAccount(id);
        if (pendingTransactionsCount > 0) {
            throw new BusinessException("Não pode desativar a conta bancária, pois ela tem " + pendingTransactionsCount + " lançamentos pendentes");
        }

        bankAccount.setActive(!bankAccount.getActive());
        bankAccountRepository.save(bankAccount);
        log.info("Bank account deactivated successfully with id: {}", id);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting bank account with id: {}", id);

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        long transactionsCount = bankAccountRepository.countTransactionsByBankAccount(id);

        if (transactionsCount > 0) {
            throw new BusinessException("Não é possível deletar a conta bancária pois ela possuí lançamentos pendentes e/ou transaões vinculadas, tente desativar");
        }

        bankAccountRepository.delete(bankAccount);
        log.info("Bank account deleted successfully with id: {}", id);
    }

    private BankAccountResponseDTO toResponseDTO(BankAccount bankAccount) {

        return BankAccountResponseDTO.builder()
                .id(bankAccount.getId())
                .name(bankAccount.getName())
                .bank(bankAccount.getBank())
                .currentBalance(bankAccount.getCurrentBalance())
                .active(bankAccount.getActive())
                .createdAt(bankAccount.getCreatedAt())
                .updatedAt(bankAccount.getUpdatedAt())
                .build();
    }
}
