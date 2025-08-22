package com.challenge.JPay.dto.request;

import com.challenge.JPay.model.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountPayableRequestDTO(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 255, message = "A descrição não pode ter mais de 255 caracteres")
    String description,

    @NotNull
    @DecimalMin(value = "0.01", message = "O valor precisa ser maior que 0")
    @Digits(integer = 10, fraction = 2, message = "O valor só pode ter no máximo 10 digitos inteiros e 2 decimais")
    BigDecimal amount,

    @NotNull(message = "A data de expiração é obrigatória")
    @Future(message = "A data de expiração precisa ser uma data futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate expirationDate,

    @NotNull(message = "A categoria é obrigatória")
    @Positive(message = "O id da categoria não atende os requisitos")
    Long categoryId,

    @NotNull(message = "A conta bancária é obrigatória")
    @Positive(message = "O id da conta bancária não atende os requisitos")
    Long bankAccountId,

    @NotEmpty(message = "O tipo da transação é obrigatório")
    String type
) { }
