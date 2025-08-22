package com.challenge.JPay.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CategoryTotalsResponseDTO(
        Long id,
        String name,
        BigDecimal value
) { }
