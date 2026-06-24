package com.scanit.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationLink = baseUrl + "/auth/verify?token=" + token;

        String htmlBody = """
                <html>
                <body>
                    <h2>Welcome to ScanIt!</h2>
                    <p>Please verify your email address by clicking the link below:</p>
                    <a href="%s" style="
                        background-color: #4F46E5;
                        color: white;
                        padding: 12px 24px;
                        text-decoration: none;
                        border-radius: 6px;
                        display: inline-block;
                        margin: 16px 0;
                    ">Verify Email</a>
                    <p>This link expires in 24 hours.</p>
                    <p>If you did not create an account, please ignore this email.</p>
                </body>
                </html>
                """.formatted(verificationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your ScanIt email address");
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }
}
