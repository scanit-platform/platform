package com.scanit.password_reset_request.service;

import com.scanit.password_reset_request.dto.PasswordResetRequestDTO;
import com.scanit.password_reset_request.exception.InvalidResetTokenException;
import com.scanit.password_reset_request.exception.PasswordValidationException;
import com.scanit.password_reset_request.model.PasswordResetRequest;
import com.scanit.password_reset_request.repository.ResetPasswordRepository;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResetPasswordServiceImpl implements  ResetPasswordService {
    private static final Duration TOKEN_EXPIRY_TIME = Duration.ofHours(1);

    private final ResetPasswordRepository resetPasswordRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordServiceImpl(ResetPasswordRepository resetPasswordRepository,
                                    UserRepository userRepository,
                                    TokenService tokenService,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.resetPasswordRepository = resetPasswordRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void requestResetPassword(String email) {
        var userEmail = userRepository.findByEmail(email);
        if (userEmail.isEmpty()) return;

        User user = userEmail.get();

        List<PasswordResetRequest> passwordResetRequests = resetPasswordRepository.findByUser(user);
        passwordResetRequests.forEach(PasswordResetRequest::markAsUsed);
        resetPasswordRepository.saveAll(passwordResetRequests);

        String token = tokenService.generateToken();

        PasswordResetRequest resetPassword = new PasswordResetRequest();
        resetPassword.setToken(token);
        resetPassword.setUser(user);
        resetPassword.setCreatedAt(LocalDateTime.now());
        resetPassword.setExpiresAt(LocalDateTime.now().plus(TOKEN_EXPIRY_TIME));

        resetPasswordRepository.save(resetPassword);
        emailService.sendResetEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequestDTO dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new PasswordValidationException("Passwords do not match");
        }

        PasswordResetRequest resetRequest = resetPasswordRepository.findByToken(dto.token())
                .orElseThrow(() -> new InvalidResetTokenException("Invalid or expired token"));

        if (!resetRequest.isUsable()) {
            throw new InvalidResetTokenException("Token has been used or has expired");
        }

        User user = resetRequest.getUser();
        user.setPassword(passwordEncoder.encode(dto.password()));
//        user.setPasswordChangedAt(LocalDateTime.now());

        userRepository.save(user);
        resetRequest.markAsUsed();
        resetPasswordRepository.save(resetRequest);

        // if using JWT, use below
        // user.setPasswordChangedAt(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateToken(String token) {
        PasswordResetRequest passwordResetRequest = resetPasswordRepository.findByToken(token)
                .orElseThrow(() -> new InvalidResetTokenException("Invalid token"));

        if (!passwordResetRequest.isUsable()) {
            throw new InvalidResetTokenException("Token has been used or has expired");
        }
    }
}
