package com.challenge.JPay.controller;

import com.challenge.JPay.dto.request.BankAccountRequestDTO;
import com.challenge.JPay.dto.response.BankAccountResponseDTO;
import com.challenge.JPay.service.BankAccountService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
@Tag(name = "Contas Bancárias", description = "Operações de gerenciamento de contas bancárias")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    @Operation(summary = "Listar contas bancárias com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas bancárias recuperadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<BankAccountResponseDTO>> getAllBankAccounts(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("GET /api/bank-accounts - Finding bank accounts with pagination: {}", pageable);

        var bankAccounts = bankAccountService.findAll(pageable);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar contas bancárias ativas")
    @ApiResponse(responseCode = "200", description = "Contas bancárias ativas recuperadas com sucesso")
    public ResponseEntity<List<BankAccountResponseDTO>> getAllActiveBankAccounts() {
        log.info("GET /api/bank-accounts/active - Finding all active bank accounts without pagination");

        var bankAccounts = bankAccountService.findAllActive();
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta bancária por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta bancária encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada")
    })
    public ResponseEntity<BankAccountResponseDTO> getBankAccountById(
            @Parameter(description = "ID da conta bancária", required = true)
            @PathVariable Long id) {
        log.info("GET /api/bank-accounts/{} - Finding bank account by id", id);

        BankAccountResponseDTO bankAccount = bankAccountService.findById(id);
        return ResponseEntity.ok(bankAccount);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar contas bancárias por banco", description = "Buscar contas bancárias por nome do banco com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas bancárias encontradas"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos")
    })
    public ResponseEntity<Page<BankAccountResponseDTO>> searchBankAccountsByBank(
            @Parameter(description = "Nome do banco para buscar", required = true)
            @RequestParam String bank,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("GET /api/bank-accounts/search - Finding bank account by name: {} with pagination: {}", bank, pageable);

        Page<BankAccountResponseDTO> bankAccounts = bankAccountService.findByBank(bank, pageable);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/total-balance")
    @Operation(summary = "Obter saldo total das contas ativas")
    @ApiResponse(responseCode = "200", description = "Saldo total recuperado com sucesso")
    public ResponseEntity<BigDecimal> getTotalBalance() {
        log.info("GET /api/bank-accounts/total-balance - Find total amount by active bank accounts");

        BigDecimal totalBalance = bankAccountService.getTotalBalance();
        return ResponseEntity.ok(totalBalance);
    }

    @PostMapping
    @Operation(summary = "Criar nova conta bancária", description = "Criar uma nova conta bancária")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta bancária criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da conta bancária inválidos"),
            @ApiResponse(responseCode = "409", description = "Conta bancária já existe")
    })
    public ResponseEntity<BankAccountResponseDTO> createBankAccount(
            @Parameter(description = "Dados da conta bancária", required = true)
            @Valid @RequestBody BankAccountRequestDTO dto) {
        log.info("POST /api/bank-accounts - Creating new bank account: {}", dto.name());

        BankAccountResponseDTO createdBankAccount = bankAccountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBankAccount);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta bancária", description = "Atualizar uma conta bancária existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta bancária atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da conta bancária inválidos"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada"),
            @ApiResponse(responseCode = "409", description = "Dados da conta bancária já existem")
    })
    public ResponseEntity<BankAccountResponseDTO> updateBankAccount(
            @Parameter(description = "ID da conta bancária", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados atualizados da conta bancária", required = true)
            @Valid @RequestBody BankAccountRequestDTO dto) {
        log.info("PUT /api/bank-accounts/{} - Updating bank account", id);

        BankAccountResponseDTO updatedBankAccount = bankAccountService.update(id, dto);
        return ResponseEntity.ok(updatedBankAccount);
    }

    @PatchMapping("/{id}/balance")
    @Operation(summary = "Atualizar saldo da conta bancária", description = "Atualizar o saldo atual de uma conta bancária")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada"),
            @ApiResponse(responseCode = "400", description = "Saldo inválido")
    })
    public ResponseEntity<BankAccountResponseDTO> updateBankAccountBalance(
            @Parameter(description = "ID da conta bancária", required = true)
            @PathVariable Long id,
            @Parameter(description = "Novo saldo atual", required = true)
            @RequestParam BigDecimal currentBalance) {
        log.info("PATCH /api/bank-accounts/{}/balance - Updating amount of bank account to: {}", id, currentBalance);

        BankAccountResponseDTO updatedBankAccount = bankAccountService.updateBalance(id, currentBalance);
        return ResponseEntity.ok(updatedBankAccount);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar conta bancária", description = "Desativar uma conta bancária (exclusão lógica)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta bancária desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada"),
            @ApiResponse(responseCode = "400", description = "Não é possível desativar conta bancária com contas pendentes")
    })
    public ResponseEntity<Void> deactivateBankAccount(
            @Parameter(description = "ID da conta bancária", required = true)
            @PathVariable Long id) {
        log.info("PATCH /api/bank-accounts/{}/deactivate - Deactivate bank account", id);

        bankAccountService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conta bancária", description = "Excluir permanentemente uma conta bancária")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta bancária excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada"),
            @ApiResponse(responseCode = "400", description = "Não é possível excluir conta bancária com contas associadas")
    })
    public ResponseEntity<Void> deleteBankAccount(
            @Parameter(description = "ID da conta bancária", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/bank-accounts/{} - Removing bank account", id);

        bankAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}