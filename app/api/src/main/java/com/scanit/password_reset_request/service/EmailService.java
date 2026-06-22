package com.scanit.password_reset_request.service;

public interface EmailService {
    void sendResetEmail(String toEmail, String token);
}
