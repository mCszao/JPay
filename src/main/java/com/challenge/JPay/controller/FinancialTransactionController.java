package com.challenge.JPay.controller;

import com.challenge.JPay.dto.response.FinancialTransactionResponseDTO;
import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.model.enums.TransactionType;
import com.challenge.JPay.service.FinancialTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Financial Transactions", description = "Operações de gerenciamento de transações financeiras")
public class FinancialTransactionController {

    private final FinancialTransactionService transactionService;

    @GetMapping
    @Operation(summary = "Listar transações com paginação", description = "Recuperar todas as transações financeiras com suporte à paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transações recuperadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<FinancialTransactionResponseDTO>> getAllTransactions(
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions - Recuperando transações com paginação: {}", pageable);
        Page<FinancialTransactionResponseDTO> transactions = transactionService.findAll(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID", description = "Recuperar uma transação específica pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação encontrada"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    public ResponseEntity<FinancialTransactionResponseDTO> getTransactionById(
            @Parameter(description = "ID da transação", required = true)
            @PathVariable Long id) {
        log.info("GET /api/transactions/{} - Recuperando transação por id", id);
        FinancialTransactionResponseDTO transaction = transactionService.findById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/bank-account/{bankAccountId}")
    @Operation(summary = "Listar transações por conta bancária", description = "Buscar transações por conta bancária com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transações encontradas"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<FinancialTransactionResponseDTO>> getTransactionsByBankAccount(
            @Parameter(description = "ID da conta bancária", required = true)
            @PathVariable Long bankAccountId,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/bank-account/{} - Buscando transações por conta bancária com paginação: {}",
                bankAccountId, pageable);
        Page<FinancialTransactionResponseDTO> transactions = transactionService.findByBankAccountId(bankAccountId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Listar transações por conta a pagar", description = "Recuperar todas as transações de uma conta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transações encontradas"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<List<FinancialTransactionResponseDTO>> getTransactionsByAccount(
            @Parameter(description = "ID da conta", required = true)
            @PathVariable Long accountId) {
        log.info("GET /api/transactions/account/{} - Recuperando transações por conta", accountId);
        List<FinancialTransactionResponseDTO> transactions = transactionService.findByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Listar transações por tipo", description = "Buscar transações por tipo com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transações encontradas"),
            @ApiResponse(responseCode = "400", description = "Tipo inválido ou parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<FinancialTransactionResponseDTO>> getTransactionsByType(
            @Parameter(description = "Tipo da transação (CREDIT, DEBIT)", required = true)
            @PathVariable String type,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/type/{} - Buscando transações por tipo com paginação: {}", type, pageable);
        Page<FinancialTransactionResponseDTO> transactions = transactionService.findByType(Enum.valueOf(TransactionType.class, type), pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Listar transações por intervalo de datas", description = "Buscar transações por intervalo de datas com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transações encontradas"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas ou parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<FinancialTransactionResponseDTO>> getTransactionsByDateRange(
            @Parameter(description = "Data e hora de início do intervalo", required = true)
            @RequestParam LocalDateTime startDate,
            @Parameter(description = "Data e hora de fim do intervalo", required = true)
            @RequestParam LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/date-range - Buscando transações por intervalo de datas: {} a {} com paginação: {}",
                startDate, endDate, pageable);
        Page<FinancialTransactionResponseDTO> transactions = transactionService.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

//    @PostMapping("/credit")
//    @Operation(summary = "Criar transação de crédito", description = "Criar uma nova transação de crédito")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Transação de crédito criada com sucesso"),
//            @ApiResponse(responseCode = "400", description = "Dados da transação inválidos"),
//            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada")
//    })
//    public ResponseEntity<FinancialTransactionResponseDTO> createCreditTransaction(
//            @Parameter(description = "ID da conta bancária", required = true)
//            @RequestParam Long bankAccountId,
//            @Parameter(description = "Valor da transação", required = true)
//            @RequestParam BigDecimal amount,
//            @Parameter(description = "Descrição da transação", required = true)
//            @RequestParam String description) {
//        log.info("POST /api/transactions/credit - Criando transação de crédito para conta bancária id: {} com valor: {}",
//                bankAccountId, amount);
//        FinancialTransactionResponseDTO transaction = transactionService.createCreditTransaction(bankAccountId, amount, description);
//        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
//    }
//
//    @PostMapping("/debit")
//    @Operation(summary = "Criar transação de débito", description = "Criar uma nova transação de débito")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Transação de débito criada com sucesso"),
//            @ApiResponse(responseCode = "400", description = "Dados da transação inválidos ou saldo insuficiente"),
//            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada")
//    })
//    public ResponseEntity<FinancialTransactionResponseDTO> createDebitTransaction(
//            @Parameter(description = "ID da conta bancária", required = true)
//            @RequestParam Long bankAccountId,
//            @Parameter(description = "Valor da transação", required = true)
//            @RequestParam BigDecimal amount,
//            @Parameter(description = "Descrição da transação", required = true)
//            @RequestParam String description) {
//        log.info("POST /api/transactions/debit - Criando transação de débito para conta bancária id: {} com valor: {}",
//                bankAccountId, amount);
//        FinancialTransactionResponseDTO transaction = transactionService.createDebitTransaction(bankAccountId, amount, description);
//        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
//    }
}