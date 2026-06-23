package com.scanit.password_reset_request.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TokenServiceImpl implements TokenService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    private static final int TOKEN_SIZE = 32;

    @Override
    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_SIZE];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
