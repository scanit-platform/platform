package com.scanit.auth;

import com.scanit.auth.dto.AuthRequest;
import com.scanit.auth.dto.AuthResponse;
import com.scanit.auth.dto.RegisterRequest;
import com.scanit.auth.dto.RegistrationResponse;
import com.scanit.auth.model.EmailVerificationToken;
import com.scanit.auth.repository.EmailVerificationTokenRepository;
import com.scanit.exception.BadRequestException;
import com.scanit.exception.ForbiddenException;
import com.scanit.exception.InvalidCredentialsException;
import com.scanit.exception.UserAlreadyExistsException;
import com.scanit.security.JwtService;
import com.scanit.security.UserPrincipal;
import com.scanit.user.dto.UserResponseDto;
import com.scanit.user.model.User;
import com.scanit.user.model.UserStatus;
import com.scanit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private static final long VERIFICATION_TOKEN_HOURS = 24;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final VerificationEmailService verificationEmailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        validatePasswordConfirmation(request);

        String normalizedEmail = normalizeEmail(request.getEmail());
        validateEmailUniqueness(normalizedEmail);

        User user = createPendingUser(request, normalizedEmail);
        User savedUser = userRepository.save(user);

        EmailVerificationToken token = createOrReplaceVerificationToken(savedUser);
        verificationEmailService.sendVerificationEmail(savedUser, token);

        return new RegistrationResponse(
                savedUser.getEmail(),
                "pending_verification",
                "Account created. Check your email to verify your account."
        );
    }

    public AuthResponse login(AuthRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizeEmail(request.getEmail()),
                            request.getPassword()
                    )
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            return buildAuthResponse(
                    principal.getId(),
                    principal.getName(),
                    principal.getEmail()
            );
        } catch (DisabledException ex) {
            throw new ForbiddenException(
                    "Account is not verified. Check your email before signing in.",
                    ex
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password", ex);
        }
    }

    @Transactional
    public AuthResponse verifyEmail(String tokenValue) {
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BadRequestException("Verification link is invalid."));

        if (token.isUsed()) {
            throw new BadRequestException("Verification link has already been used.");
        }

        if (token.isExpired(now)) {
            throw new BadRequestException("Verification link expired. Request a new verification email.");
        }

        User user = token.getUser();
        user.setStatus(UserStatus.ACTIVE);
        token.setUsedAt(now);

        return buildAuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    @Transactional
    public RegistrationResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new BadRequestException("No account found for this email."));

        if (user.getStatus() == UserStatus.ACTIVE) {
            return new RegistrationResponse(
                    user.getEmail(),
                    "active",
                    "This account is already verified. You can sign in."
            );
        }

        EmailVerificationToken token = createOrReplaceVerificationToken(user);
        verificationEmailService.sendVerificationEmail(user, token);

        return new RegistrationResponse(
                user.getEmail(),
                "pending_verification",
                "A new verification email has been sent."
        );
    }

    public UserResponseDto getCurrentUser(UserPrincipal currentUser) {
        return new UserResponseDto(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getEmail()
        );
    }

    private User createPendingUser(RegisterRequest request, String normalizedEmail) {
        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();
        String fullName = firstName + " " + lastName;

        return new User(
                null,
                fullName,
                firstName,
                lastName,
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                UserStatus.PENDING_VERIFICATION,
                LocalDateTime.now()
        );
    }

    private EmailVerificationToken createOrReplaceVerificationToken(User user) {
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken token = emailVerificationTokenRepository.findByUser(user)
                .orElseGet(() -> new EmailVerificationToken(
                        null,
                        null,
                        user,
                        null,
                        null,
                        null
                ));

        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(now.plusHours(VERIFICATION_TOKEN_HOURS));
        token.setUsedAt(null);
        token.setCreatedAt(now);

        return emailVerificationTokenRepository.save(token);
    }

    private void validatePasswordConfirmation(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match.");
        }
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponse buildAuthResponse(Long id, String name, String email) {
        String token = jwtService.generateToken(id, email);

        return new AuthResponse(
                id,
                name,
                email,
                token,
                jwtService.extractExpiration(token)
        );
    }
}
