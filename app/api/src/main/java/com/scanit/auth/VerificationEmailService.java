package com.scanit.auth;

import com.scanit.auth.model.EmailVerificationToken;
import com.scanit.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationEmailService {
    private final String webBaseUrl;

    public VerificationEmailService(@Value("${app.base-url:http://localhost:3000}") String webBaseUrl) {
        this.webBaseUrl = webBaseUrl.replaceAll("/+$", "");
    }

    public void sendVerificationEmail(User user, EmailVerificationToken verificationToken) {
        String verificationLink = webBaseUrl + "/verify-email?token=" + verificationToken.getToken();

        log.info("Verification email would be sent to {}. Link: {}", user.getEmail(), verificationLink);
    }
}
