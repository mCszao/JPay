package com.challenge.JPay.config;
import com.challenge.JPay.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalCustomExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        GlobalErrorResponse errorResponse = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erro de validação")
                .message("Entrada de dados inválida")
                .validationErrors(errors)
                .build();
        log.warn("Errors: {}", errors);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ResourceDuplicateException.class)
    public ResponseEntity<GlobalErrorResponse> handleResourceDuplicateException(ResourceDuplicateException ex) {
        GlobalErrorResponse errorResponse = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Recurso não encontrado")
                .message(ex.getMessage())
                .build();
        log.warn("Resource duplicate: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        GlobalErrorResponse errorResponse = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Recurso não encontrado")
                .message(ex.getMessage())
                .build();
        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleCategoyNotFoundException(CategoryNotFoundException ex) {
        GlobalErrorResponse errorResponse = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Recurso não encontrado")
                .message(ex.getMessage())
                .build();

        log.warn("Category not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AccountPayableNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleCategoyNotFoundException(AccountPayableNotFoundException ex) {
        GlobalErrorResponse errorResponse = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Recurso não encontrado")
                .message(ex.getMessage())
                .build();

        log.warn("Account not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalErrorResponse> handleBusinessException(BusinessException ex) {
        GlobalErrorResponse errorResponse = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Regra de negócio não atendida")
                .message(ex.getMessage())
                .build();
        log.warn("Business exception: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleGenericException(Exception ex) {
        GlobalErrorResponse errorResponse = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .build();
        log.error("ERRO INTERNO INESPERADO: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}