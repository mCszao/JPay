package com.challenge.JPay.dto.response;

import com.challenge.JPay.model.enums.Status;
import com.challenge.JPay.model.enums.TransactionType;
import com.challenge.JPay.service.CategoryService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record AccountPayableResponseDTO(
        Long id,
        String description,
        BigDecimal amount,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expirationDate,

        @JsonFormat(pattern = "yyyy-MM-dd")

        LocalDate paymentDate,
        TransactionType type,
        Status status,
        boolean isExpired,
        CategoryResponseDTO category,
        BankAccountResponseDTO bankAccount,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt


) { }
