package com.scanit.password_reset_request.service;

import io.awspring.cloud.ses.SimpleEmailServiceMailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final SimpleEmailServiceMailSender emailSender;
    private final String fromEmail;
    private final boolean deliveryEnabled;
    private final String webBaseUrl;

    public EmailServiceImpl(
            SimpleEmailServiceMailSender emailSender,
            @Value("${app.email.from}") String fromEmail,
            @Value("${app.email.delivery-enabled:false}") boolean deliveryEnabled,
            @Value("${app.base-url:http://localhost:3000}") String webBaseUrl) {
        this.emailSender = emailSender;
        this.fromEmail = fromEmail;
        this.deliveryEnabled = deliveryEnabled;
        this.webBaseUrl = webBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public void sendResetEmail(String toEmail, String token) {
        String resetUrl = webBaseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below.\n\n" + resetUrl);

        if (deliveryEnabled) {
            emailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
            return;
        }

        log.info("Password reset email would be sent to {}. Link: {}", toEmail, resetUrl);
    }
}
