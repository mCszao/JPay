package com.challenge.JPay.controller;

import com.challenge.JPay.dto.request.TransactionRequestDTO;
import com.challenge.JPay.dto.request.PaymentRequestDTO;
import com.challenge.JPay.dto.response.TransactionResponseDTO;
import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Lançamentos", description = "Operações de gerenciamento dos lançamentos")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Listar lançamentos com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamentos recuperados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<TransactionResponseDTO>> getAllTransactions(
            @PageableDefault(size = 20, sort = "expirationDate", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/transactions - Finding transactions with pagination: {}", pageable);

        var transactions = transactionService.findAll(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
    })
    public ResponseEntity<TransactionResponseDTO> getTransactionById(
            @Parameter(description = "ID do lançamento", required = true)
            @PathVariable Long id) {
        log.info("GET /api/transactions/{} - Finding account by id", id);

        var transaction = transactionService.findById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar lançamentos por status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamentos encontrados"),
            @ApiResponse(responseCode = "400", description = "Status inválido ou parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactionsByStatus(
            @Parameter(description = "Status do lançamento (PENDING, PAID)", required = true)
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        log.info("GET /api/transactions/status/{} - Finding transactions by status: {}", status, pageable);

        var transactions = transactionService.findByStatus(Enum.valueOf(Status.class, status), pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/isExpired")
    @Operation(summary = "Listar lançamentos vencidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamentos vencidos recuperados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<TransactionResponseDTO>> getExpiredTransactions(
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        log.info("GET /api/transactions/isExpired - Finding expired transactions with pagination: {}", pageable);

        var transactions = transactionService.findByExpiredTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/due-date-range")
    @Operation(summary = "Listar lançamentos por intervalo de vencimento", description = "Buscar lançamentos por intervalo de data de vencimento com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamentos encontrados"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas ou parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactionsByExpirationDateRange(
            @Parameter(description = "Data de início do intervalo", required = true)
            @RequestParam LocalDate startDate,
            @Parameter(description = "Data de fim do intervalo", required = true)
            @RequestParam LocalDate endDate,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        log.info("GET /api/transactions/due-date-range - Finding transactions by expiration date between: {} to {} with pagination: {}",
                startDate, endDate, pageable);


        var transactions = transactionService.findByExpirationDateBetween(startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/expired-date-range-type")
    @Operation(summary = "Listar lançamentos por intervalo de vencimento", description = "Buscar lançamentos por intervalo de data de vencimento com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamentos encontrados"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas ou parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactionsByExpirationDateRangeAndType(
            @Parameter(description = "Data de início do intervalo", required = true)
            @RequestParam LocalDate startDate,
            @Parameter(description = "Data de fim do intervalo", required = true)
            @RequestParam LocalDate endDate,
            @Parameter(description = "Tipo dos lançamentos a serem filtradas", required = false)
            @RequestParam String type,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        log.info("GET /api/transactions/expired-date-range-type - Finding transactions by expiration date between: {} to {} and type {} with pagination: {}",
                startDate, endDate, type, pageable);


        var transactions = transactionService.findByExpirationDateBetweenAndType(startDate, endDate, type, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/total-amount")
    @Operation(summary = "Retorna o total dos lançamentos por tipo")
    @ApiResponse(responseCode = "200", description = "Total retornado com sucesso")
    public ResponseEntity<BigDecimal> getTotalAmount(
        @Parameter(description = "Tipo do lançamento", required = true)
        @RequestParam String type
    ) {
        log.info("GET /api/total-amount - Searching total amount by type");

        var total = transactionService.getTotalAmountByType(type);
        return ResponseEntity.ok(total);
    }

    @PostMapping
    @Operation(summary = "Criar novo lançamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lançamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do lançamento inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoria ou conta bancária não encontrada")
    })
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Parameter(description = "Dados do lançamento", required = true)
            @Valid @RequestBody TransactionRequestDTO dto) {
        log.info("POST /api/transactions - Creating new transaction: {}", dto.description());

        TransactionResponseDTO createdTransaction = transactionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lançamento", description = "Atualizar um lançamento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do lançamento inválidos"),
            @ApiResponse(responseCode = "404", description = "Lançamento, categoria ou conta bancária não encontrados")
    })
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @Parameter(description = "ID do lançamento", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados atualizados do lançamento", required = true)
            @Valid @RequestBody TransactionRequestDTO dto) {
        log.info("PUT /api/transactions/{} - Updating transaction", id);
        var updatedTransaction = transactionService.update(id, dto);
        return ResponseEntity.ok(updatedTransaction);
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Pagar lançamento por ID", description = "Realizar o pagamento de um lançamento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamento pago com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de pagamento inválidos"),
            @ApiResponse(responseCode = "404", description = "Conta ou conta bancária não encontrada")
    })
    public ResponseEntity<TransactionResponseDTO> payTransactionById(
            @Parameter(description = "ID do lançamento", required = true)
            @PathVariable Long id) {
        log.info("POST /api/transactions/{}/pay - Paying transactions by id", id);

        var paidTransaction = transactionService.payTransaction(id);
        return ResponseEntity.ok(paidTransaction);
    }

    @PostMapping("/pay")
    @Operation(summary = "Pagar lançamento", description = "Realizar o pagamento de um lançamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lançamento pago com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de pagamento inválidos"),
            @ApiResponse(responseCode = "404", description = "Lançamento ou conta bancária não encontrada")
    })
    public ResponseEntity<TransactionResponseDTO> payTransaction(
            @Parameter(description = "Dados do pagamento", required = true)
            @Valid @RequestBody PaymentRequestDTO dto) {
        log.info("POST /api/transactions/pay - Paying transactions id: {}", dto.transactionId());

        var paidTransaction = transactionService.payTransaction(dto);
        return ResponseEntity.ok(paidTransaction);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lançamento", description = "Excluir permanentemente um lançamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lançamento excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lançamento não encontrada"),
    })
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "ID do lançamento", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/transactions/{} - Removing transaction", id);
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}