package com.challenge.JPay.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BankAccountRequestDTO(
        @NotBlank(message = "O nome da conta bancária é obrigatório")
        @Size(max = 100, message = "O nome da conta bancária não pode ter mais de 100 caracteres")
        String name,

        @NotBlank(message = "O nome do banco é obrigatório")
        @Size(max = 100, message = "O nome do banco não pode ter mais de 100 caracteres")
        String bank,

        @DecimalMin(value = "0.0", inclusive = true, message = "O saldo atual precisa ser maior ou igual a 0")
        @Digits(integer = 10, fraction = 2, message = "O saldo atual só pode ter no máximo 10 digitos inteiros e 2 decimais")
        BigDecimal currentBalance


) { }
