package com.scanit.auth;

import com.scanit.auth.dto.AuthRequest;
import com.scanit.auth.dto.AuthResponse;
import com.scanit.auth.dto.MessageResponse;
import com.scanit.auth.dto.RegisterRequest;
import com.scanit.auth.dto.ResetPasswordRequest;
import com.scanit.exception.BadRequestException;
import com.scanit.exception.InvalidCredentialsException;
import com.scanit.security.JwtService;
import com.scanit.security.UserPrincipal;
import com.scanit.user.dto.UserRequestDto;
import com.scanit.user.dto.UserResponseDto;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import com.scanit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName(request.getName());
        userRequestDto.setEmail(request.getEmail());
        userRequestDto.setPassword(request.getPassword());

        UserResponseDto user = userService.registerUser(userRequestDto);
        return buildAuthResponse(user.getId(), user.getName(), user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return buildAuthResponse(principal.getId(), principal.getName(), principal.getEmail());
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password", ex);
        }
    }

    public UserResponseDto getCurrentUser(UserPrincipal currentUser) {
        return new UserResponseDto(currentUser.getId(), currentUser.getName(), currentUser.getEmail());
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }

        String tokenHash = hashToken(request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("The reset link is invalid or expired"));

        LocalDateTime now = LocalDateTime.now();

        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(now)) {
            throw new BadRequestException("The reset link is invalid or expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordChangedAt(now);

        resetToken.setUsedAt(now);

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("Password has been reset successfully");
    }

    private AuthResponse buildAuthResponse(Long id, String name, String email) {
        String token = jwtService.generateToken(id, email);
        return new AuthResponse(id, name, email, token, jwtService.extractExpiration(token));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}