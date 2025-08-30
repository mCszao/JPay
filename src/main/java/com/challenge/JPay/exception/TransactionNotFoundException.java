package com.challenge.JPay.exception;

public class TransactionNotFoundException extends RuntimeException {
  public TransactionNotFoundException(String message) {
    super(message);
  }

    public TransactionNotFoundException(long id) {
        super(String.format("ID do lançamento não foi encontrado na base de dados (ID:  %s)", id));
    }
}
