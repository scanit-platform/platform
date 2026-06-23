package com.scanit.password_reset_request.exception;

public class InvalidResetTokenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidResetTokenException(String message) {
        super(message);
    }
}
