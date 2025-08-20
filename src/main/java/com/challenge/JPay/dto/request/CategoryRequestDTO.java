package com.challenge.JPay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(
        @NotBlank(message = "O nome da categoria é obrigatório")
        @Size(max = 100, message = "O nome da categoria não pode ter mais de 100 caracteres")
        String name,

        @Size(max = 255, message = "A descrição da categoria não pode ter mais de 255 caracteres")
        String description
) { }
