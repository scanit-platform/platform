package com.scanit.auth;

import com.scanit.auth.dto.RegisterRequest;
import com.scanit.auth.dto.RegistrationResponse;
import com.scanit.auth.model.EmailVerificationToken;
import com.scanit.auth.repository.EmailVerificationTokenRepository;
import com.scanit.exception.BadRequestException;
import com.scanit.exception.UserAlreadyExistsException;
import com.scanit.security.JwtService;
import com.scanit.user.model.User;
import com.scanit.user.model.UserStatus;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceEdgeCaseTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private VerificationEmailService verificationEmailService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                emailVerificationTokenRepository,
                verificationEmailService,
                authenticationManager,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void shouldRejectRegistrationWhenPasswordConfirmationDoesNotMatch() {
        RegisterRequest request = registerRequest(
                "Dana",
                "Test",
                "dana@example.com",
                "Password1",
                "Password2"
        );

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password and confirm password do not match.");

        verifyNoInteractions(
                userRepository,
                passwordEncoder,
                emailVerificationTokenRepository,
                verificationEmailService
        );
    }

    @Test
    void shouldRejectRegistrationWhenNormalizedEmailAlreadyExists() {
        RegisterRequest request = registerRequest(
                "Dana",
                "Test",
                "  Dana@Example.COM  ",
                "Password1",
                "Password1"
        );

        when(userRepository.existsByEmail("dana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email dana@example.com already exists");

        verify(userRepository).existsByEmail("dana@example.com");
        verifyNoInteractions(passwordEncoder, emailVerificationTokenRepository, verificationEmailService);
    }

    @Test
    void shouldNormalizeEmailDuringRegistration() {
        RegisterRequest request = registerRequest(
                "Dana",
                "Test",
                "  Dana.Test@Example.COM  ",
                "Password1",
                "Password1"
        );

        when(userRepository.existsByEmail("dana.test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(emailVerificationTokenRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("dana.test@example.com");
        assertThat(response.getEmail()).isEqualTo("dana.test@example.com");
        assertThat(response.getStatus()).isEqualTo("pending_verification");
    }

    @Test
    void shouldCreateVerificationTokenThatExpiresInApproximatelyTwentyFourHours() {
        RegisterRequest request = registerRequest(
                "Dana",
                "Test",
                "dana@example.com",
                "Password1",
                "Password1"
        );

        LocalDateTime beforeRegistration = LocalDateTime.now();

        when(userRepository.existsByEmail("dana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(emailVerificationTokenRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        LocalDateTime afterRegistration = LocalDateTime.now();

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken token = tokenCaptor.getValue();

        assertThat(token.getExpiresAt())
                .isAfterOrEqualTo(beforeRegistration.plusHours(24))
                .isBeforeOrEqualTo(afterRegistration.plusHours(24));

        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getCreatedAt()).isNotNull();
        assertThat(token.getUsedAt()).isNull();
    }

    @Test
    void shouldTrimFirstNameAndLastNameAndBuildFullNameDuringRegistration() {
        RegisterRequest request = registerRequest(
                "  Dana  ",
                "  Test  ",
                "dana@example.com",
                "Password1",
                "Password1"
        );

        when(userRepository.existsByEmail("dana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(emailVerificationTokenRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstName()).isEqualTo("Dana");
        assertThat(savedUser.getLastName()).isEqualTo("Test");
        assertThat(savedUser.getName()).isEqualTo("Dana Test");
    }

    @Test
    void shouldRejectAlreadyUsedVerificationToken() {
        User user = pendingUser();

        EmailVerificationToken token = new EmailVerificationToken(
                10L,
                "used-token",
                user,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().minusHours(1)
        );

        when(emailVerificationTokenRepository.findByToken("used-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail("used-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Verification link has already been used.");

        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectResendVerificationForUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resendVerificationEmail("  Missing@Example.COM  "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("No account found for this email.");

        verify(userRepository).findByEmail("missing@example.com");
        verifyNoInteractions(emailVerificationTokenRepository, verificationEmailService);
    }

    @Test
    void shouldNotCreateVerificationTokenWhenResendingForActiveUser() {
        User activeUser = new User(
                1L,
                "Dana Test",
                "Dana",
                "Test",
                "dana@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                LocalDateTime.now().minusDays(1)
        );

        when(userRepository.findByEmail("dana@example.com")).thenReturn(Optional.of(activeUser));

        RegistrationResponse response = authService.resendVerificationEmail("dana@example.com");

        assertThat(response.getEmail()).isEqualTo("dana@example.com");
        assertThat(response.getStatus()).isEqualTo("active");
        assertThat(response.getMessage()).isEqualTo("This account is already verified. You can sign in.");

        verifyNoInteractions(emailVerificationTokenRepository, verificationEmailService);
    }

    @Test
    void shouldClearUsedAtAndReplaceTokenWhenResendingUsedVerificationToken() {
        User user = pendingUser();

        EmailVerificationToken usedToken = new EmailVerificationToken(
                10L,
                "old-used-token",
                user,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().minusHours(25)
        );

        when(userRepository.findByEmail("dana@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationTokenRepository.findByUser(user)).thenReturn(Optional.of(usedToken));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationResponse response = authService.resendVerificationEmail("dana@example.com");

        assertThat(usedToken.getToken()).isNotBlank();
        assertThat(usedToken.getToken()).isNotEqualTo("old-used-token");
        assertThat(usedToken.getUsedAt()).isNull();
        assertThat(usedToken.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(usedToken.getCreatedAt()).isNotNull();

        verify(verificationEmailService).sendVerificationEmail(user, usedToken);

        assertThat(response.getEmail()).isEqualTo("dana@example.com");
        assertThat(response.getStatus()).isEqualTo("pending_verification");
        assertThat(response.getMessage()).isEqualTo("A new verification email has been sent.");
    }

    private RegisterRequest registerRequest(
            String firstName,
            String lastName,
            String email,
            String password,
            String confirmPassword
    ) {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private User pendingUser() {
        return new User(
                1L,
                "Dana Test",
                "Dana",
                "Test",
                "dana@example.com",
                "encoded-password",
                UserStatus.PENDING_VERIFICATION,
                LocalDateTime.now().minusMinutes(10)
        );
    }
}