package com.challenge.JPay.exception;

public class FinancialTransactionNotFoundException extends RuntimeException {
    public FinancialTransactionNotFoundException(String message) {
        super(message);
    }

    public FinancialTransactionNotFoundException(long id) {
        super(String.format("ID da movimentação não foi encontrado na base de dados (ID:  %s)", id));
    }
}
