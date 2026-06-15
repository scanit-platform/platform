package com.scanit.password_reset_request.service;

import io.awspring.cloud.ses.SimpleEmailServiceMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final SimpleEmailServiceMailSender emailSender;
    private final String fromEmail;

    public EmailServiceImpl(SimpleEmailServiceMailSender  emailSender,
                            @Value("${app.email.from}") String fromEmail) {
        this.emailSender = emailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendResetEmail(String toEmail, String token) {
        String resetUrl = "https://scanit.com/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below." + resetUrl);

        emailSender.send(message);
    }
}
