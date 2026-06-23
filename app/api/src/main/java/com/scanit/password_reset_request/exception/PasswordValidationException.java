package com.scanit.password_reset_request.exception;

public class PasswordValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PasswordValidationException(String message) {
        super(message);
    }
}
