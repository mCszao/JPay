package com.challenge.JPay.exception;

public class BankAccountNotFoundException extends RuntimeException {
    public BankAccountNotFoundException(String message) {
        super(message);
    }

  public BankAccountNotFoundException(long id) {
    super(String.format("ID da conta bancária não foi encontrada na base de dados (ID:  %s)", id));
  }
}
