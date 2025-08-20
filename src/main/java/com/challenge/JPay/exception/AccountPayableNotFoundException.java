package com.challenge.JPay.exception;

public class AccountPayableNotFoundException extends RuntimeException {
  public AccountPayableNotFoundException(String message) {
    super(message);
  }

    public AccountPayableNotFoundException(long id) {
        super(String.format("ID da conta a pagar não foi encontrada na base de dados (ID:  %s)", id));
    }
}
