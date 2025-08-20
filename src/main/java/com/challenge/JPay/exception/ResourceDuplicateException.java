package com.challenge.JPay.exception;

public class ResourceDuplicateException extends RuntimeException {
    public ResourceDuplicateException(String message) {
        super(message);
    }
    public ResourceDuplicateException(String field, String message) {
         super(String.join(field, message));
    }
}
