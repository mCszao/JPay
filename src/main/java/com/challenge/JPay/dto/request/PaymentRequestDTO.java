package com.challenge.JPay.dto.request;

import com.challenge.JPay.model.enums.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record PaymentRequestDTO(
        @NotNull(message = "A conta é obrigatória")
        @Positive(message = "O id da conta não atende os requisitos")
        Long accountId,

        @NotNull(message = "A conta bancária é obrigatória")
        @Positive(message = "O id da conta bancária não atende os requisitos")
        Long bankAccountId,

        @NotEmpty(message = "O tipo da transação é obrigatório")
        TransactionType type

) { }
