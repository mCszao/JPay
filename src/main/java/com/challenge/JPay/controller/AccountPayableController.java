package com.challenge.JPay.controller;

import com.challenge.JPay.dto.request.AccountPayableRequestDTO;
import com.challenge.JPay.dto.request.PaymentRequestDTO;
import com.challenge.JPay.dto.response.AccountPayableResponseDTO;
import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.service.AccountPayableService;
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

        import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/accounts-payable")
@RequiredArgsConstructor
@Tag(name = "Contas a Pagar", description = "Operações de gerenciamento de contas a pagar")
public class AccountPayableController {

    private final AccountPayableService accountService;

    @GetMapping
    @Operation(summary = "Listar contas com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas recuperadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<AccountPayableResponseDTO>> getAllAccounts(
            @PageableDefault(size = 20, sort = "expirationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/accounts-payable - Finding accounts with pagination: {}", pageable);

        var accounts = accountService.findAll(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<AccountPayableResponseDTO> getAccountById(
            @Parameter(description = "ID da conta", required = true)
            @PathVariable Long id) {
        log.info("GET /api/accounts-payable/{} - Finding account by id", id);

        var account = accountService.findById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar contas por status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas encontradas"),
            @ApiResponse(responseCode = "400", description = "Status inválido ou parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<AccountPayableResponseDTO>> getAccountsByStatus(
            @Parameter(description = "Status da conta (PENDING, PAID)", required = true)
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        log.info("GET /api/accounts-payable/status/{} - Finding accounts by status: {}", status, pageable);

        var accounts = accountService.findByStatus(Enum.valueOf(Status.class, status), pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/isExpired")
    @Operation(summary = "Listar contas vencidas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas vencidas recuperadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<AccountPayableResponseDTO>> getExpiredAccounts(
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        log.info("GET /api/accounts-payable/isExpired - Finding expired accounts with pagination: {}", pageable);

        var accounts = accountService.findOverdueAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/due-date-range")
    @Operation(summary = "Listar contas por intervalo de vencimento", description = "Buscar contas por intervalo de data de vencimento com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas encontradas"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas ou parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<AccountPayableResponseDTO>> getAccountsByExpirationDateRange(
            @Parameter(description = "Data de início do intervalo", required = true)
            @RequestParam LocalDate startDate,
            @Parameter(description = "Data de fim do intervalo", required = true)
            @RequestParam LocalDate endDate,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        log.info("GET /api/accounts-payable/due-date-range - Finding accounts by expiration date between: {} to {} with pagination: {}",
                startDate, endDate, pageable);

        var accounts = accountService.findByExpirationDateBetween(startDate, endDate, pageable);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    @Operation(summary = "Criar nova conta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da conta inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoria ou conta bancária não encontrada")
    })
    public ResponseEntity<AccountPayableResponseDTO> createAccount(
            @Parameter(description = "Dados da conta", required = true)
            @Valid @RequestBody AccountPayableRequestDTO dto) {
        log.info("POST /api/accounts-payable - Creating new account: {}", dto.description());

        AccountPayableResponseDTO createdAccount = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta", description = "Atualizar uma conta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da conta inválidos"),
            @ApiResponse(responseCode = "404", description = "Conta, categoria ou conta bancária não encontrada")
    })
    public ResponseEntity<AccountPayableResponseDTO> updateAccount(
            @Parameter(description = "ID da conta", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados atualizados da conta", required = true)
            @Valid @RequestBody AccountPayableRequestDTO dto) {
        log.info("PUT /api/accounts-payable/{} - Updating account", id);
        var updatedAccount = accountService.update(id, dto);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Pagar conta por ID", description = "Realizar o pagamento de uma conta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta paga com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de pagamento inválidos ou conta já paga"),
            @ApiResponse(responseCode = "404", description = "Conta ou conta bancária não encontrada")
    })
    public ResponseEntity<AccountPayableResponseDTO> payAccountById(
            @Parameter(description = "ID da conta", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados do pagamento", required = true)
            @Valid @RequestBody PaymentRequestDTO dto) {
        log.info("POST /api/accounts-payable/{}/pay - Paying account by id", id);

        var newDto = PaymentRequestDTO.builder()
                .accountId(id)
                .bankAccountId(dto.bankAccountId());
        var paidAccount = accountService.payAccount(dto);
        return ResponseEntity.ok(paidAccount);
    }

    @PostMapping("/pay")
    @Operation(summary = "Pagar conta", description = "Realizar o pagamento de uma conta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta paga com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de pagamento inválidos ou conta já paga"),
            @ApiResponse(responseCode = "404", description = "Conta ou conta bancária não encontrada")
    })
    public ResponseEntity<AccountPayableResponseDTO> payAccount(
            @Parameter(description = "Dados do pagamento", required = true)
            @Valid @RequestBody PaymentRequestDTO dto) {
        log.info("POST /api/accounts-payable/pay - Paying account id: {}", dto.accountId());

        var paidAccount = accountService.payAccount(dto);
        return ResponseEntity.ok(paidAccount);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conta", description = "Excluir permanentemente uma conta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
    })
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "ID da conta", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/accounts-payable/{} - Removing conta", id);
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}