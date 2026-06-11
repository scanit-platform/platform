package com.scanit.exception;

public class PasswordStrengthException extends RuntimeException {
        public PasswordStrengthException(String message) {
            super(message);
        }
}
