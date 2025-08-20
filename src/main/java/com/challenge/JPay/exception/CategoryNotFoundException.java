package com.challenge.JPay.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(long id) {
        super(String.format("ID da categoria não foi encontrada na base de dados (ID:  %s)", id));
    }
}
