package com.challenge.JPay.config;

import com.challenge.JPay.model.AccountPayable;
import com.challenge.JPay.model.BankAccount;
import com.challenge.JPay.model.Category;
import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.model.enums.TransactionType;
import com.challenge.JPay.repository.AccountPayableRepository;
import com.challenge.JPay.repository.BankAccountRepository;
import com.challenge.JPay.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountPayableRepository accountRepository;

    @Override
    public void run(String... args) {
        log.info("Iniciando DataLoader para popular o banco H2...");

        // Verificar se já existem dados para evitar duplicação
        if (categoryRepository.count() > 0) {
            log.info("Dados já existem no banco. Pulando DataLoader.");
            return;
        }

        // ========== CATEGORIAS ==========
        Category utilities = categoryRepository.save(Category.builder()
                .name("Utilidades")
                .description("Contas de água, luz, gás e telefone")
                .active(true)
                .build());

        Category food = categoryRepository.save(Category.builder()
                .name("Alimentação")
                .description("Supermercado, restaurantes e delivery")
                .active(true)
                .build());

        Category transport = categoryRepository.save(Category.builder()
                .name("Transporte")
                .description("Combustível, manutenção e transporte público")
                .active(true)
                .build());

        Category health = categoryRepository.save(Category.builder()
                .name("Saúde")
                .description("Plano de saúde, medicamentos e consultas")
                .active(true)
                .build());

        Category inactive = categoryRepository.save(Category.builder()
                .name("Categoria Inativa")
                .description("Categoria desativada para testes")
                .active(false)
                .build());

        log.info("Categorias criadas: {}", categoryRepository.count());

        // ========== CONTAS BANCÁRIAS ==========
        BankAccount itau = bankAccountRepository.save(BankAccount.builder()
                .name("Conta Corrente Itaú")
                .bank("Itaú")
                .currentBalance(new BigDecimal("5000.00"))
                .active(true)
                .build());

        BankAccount nubank = bankAccountRepository.save(BankAccount.builder()
                .name("Conta Nubank")
                .bank("Nubank")
                .currentBalance(new BigDecimal("2500.00"))
                .active(true)
                .build());

        BankAccount bradesco = bankAccountRepository.save(BankAccount.builder()
                .name("Poupança Bradesco")
                .bank("Bradesco")
                .currentBalance(new BigDecimal("1200.00"))
                .active(true)
                .build());

        BankAccount inactiveBank = bankAccountRepository.save(BankAccount.builder()
                .name("Conta Desativada")
                .bank("Banco Teste")
                .currentBalance(BigDecimal.ZERO)
                .active(false)
                .build());

        log.info("Contas bancárias criadas: {}", bankAccountRepository.count());

        // ========== CONTAS A PAGAR/RECEBER ==========

        // Contas PENDENTES (futuras)
        accountRepository.save(AccountPayable.builder()
                .description("Conta de Luz - Agosto 2025")
                .amount(new BigDecimal("145.80"))
                .expirationDate(LocalDate.now().plusDays(10))
                .status(Status.PENDING)
                .transactionType(TransactionType.PASSIVO)
                .category(utilities)
                .bankAccount(itau)
                .build());

        accountRepository.save(AccountPayable.builder()
                .description("Supermercado Extra")
                .amount(new BigDecimal("320.50"))
                .expirationDate(LocalDate.now().plusDays(5))
                .status(Status.PENDING)
                .transactionType(TransactionType.PASSIVO)
                .category(food)
                .bankAccount(nubank)
                .build());

        accountRepository.save(AccountPayable.builder()
                .description("Plano de Saúde - Setembro")
                .amount(new BigDecimal("450.00"))
                .expirationDate(LocalDate.now().plusDays(15))
                .status(Status.PENDING)
                .transactionType(TransactionType.PASSIVO)
                .category(health)
                .bankAccount(itau)
                .build());

        // Contas VENCIDAS (overdue)
        accountRepository.save(AccountPayable.builder()
                .description("Combustível - Posto Shell")
                .amount(new BigDecimal("180.00"))
                .expirationDate(LocalDate.now().minusDays(3))
                .status(Status.PENDING)
                .transactionType(TransactionType.PASSIVO)
                .category(transport)
                .bankAccount(nubank)
                .build());

        accountRepository.save(AccountPayable.builder()
                .description("Conta de Água - Julho")
                .amount(new BigDecimal("89.30"))
                .expirationDate(LocalDate.now().minusDays(7))
                .status(Status.PENDING)
                .transactionType(TransactionType.PASSIVO)
                .category(utilities)
                .bankAccount(bradesco)
                .build());

        // Contas PAGAS
        accountRepository.save(AccountPayable.builder()
                .description("Conta de Luz - Julho 2025")
                .amount(new BigDecimal("132.45"))
                .expirationDate(LocalDate.now().minusDays(20))
                .paymentDate(LocalDate.now().minusDays(18))
                .status(Status.PAID)
                .transactionType(TransactionType.PASSIVO)
                .category(utilities)
                .bankAccount(itau)
                .build());

        accountRepository.save(AccountPayable.builder()
                .description("Farmácia - Medicamentos")
                .amount(new BigDecimal("67.90"))
                .expirationDate(LocalDate.now().minusDays(15))
                .paymentDate(LocalDate.now().minusDays(12))
                .status(Status.PAID)
                .transactionType(TransactionType.PASSIVO)
                .category(health)
                .bankAccount(nubank)
                .build());

        accountRepository.save(AccountPayable.builder()
                .description("Restaurante - Almoço Executivo")
                .amount(new BigDecimal("45.00"))
                .expirationDate(LocalDate.now().minusDays(10))
                .paymentDate(LocalDate.now().minusDays(10))
                .status(Status.PAID)
                .transactionType(TransactionType.PASSIVO)
                .category(food)
                .bankAccount(bradesco)
                .build());

        // Algumas contas de CRÉDITO (recebimentos)
        accountRepository.save(AccountPayable.builder()
                .description("Reembolso Plano de Saúde")
                .amount(new BigDecimal("200.00"))
                .expirationDate(LocalDate.now().plusDays(2))
                .status(Status.PENDING)
                .transactionType(TransactionType.ATIVO)
                .category(health)
                .bankAccount(itau)
                .build());

        accountRepository.save(AccountPayable.builder()
                .description("Cashback Cartão de Crédito")
                .amount(new BigDecimal("85.50"))
                .expirationDate(LocalDate.now().minusDays(5))
                .paymentDate(LocalDate.now().minusDays(3))
                .status(Status.PAID)
                .transactionType(TransactionType.ATIVO)
                .category(food)
                .bankAccount(nubank)
                .build());

        log.info("Contas criadas: {}", accountRepository.count());

        // ========== RESUMO ==========
        log.info("=== RESUMO DOS DADOS CRIADOS ===");
        log.info("Categorias: {} (4 ativas, 1 inativa)", categoryRepository.count());
        log.info("Contas Bancárias: {} (3 ativas, 1 inativa)", bankAccountRepository.count());
        log.info("Contas: {}", accountRepository.count());
        log.info("DataLoader concluído com sucesso!");
    }
}