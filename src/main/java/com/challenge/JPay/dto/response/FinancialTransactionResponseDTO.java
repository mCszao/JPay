package com.challenge.JPay.dto.response;

import com.challenge.JPay.model.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FinancialTransactionResponseDTO(
    Long id,
    TransactionType type,
    BigDecimal amount,
    String description,
    BigDecimal previousBalance,
    BigDecimal currentBalance,

    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    LocalDateTime transactionDate,
    Long bankAccountId,
    String bankAccountName,
    Long accountId,
    String accountDescription
) { }
