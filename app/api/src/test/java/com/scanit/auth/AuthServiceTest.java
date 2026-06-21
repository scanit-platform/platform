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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
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

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesPendingUserAndSendsVerificationEmail() {
        RegisterRequest request = validRegisterRequest();

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(emailVerificationTokenRepository.findByUser(any(User.class)))
                .thenReturn(Optional.empty());

        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("John Doe", savedUser.getName());
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(UserStatus.PENDING_VERIFICATION, savedUser.getStatus());
        assertNotNull(savedUser.getCreatedAt());

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken token = tokenCaptor.getValue();

        assertNotNull(token.getToken());
        assertEquals(savedUser, token.getUser());
        assertNull(token.getUsedAt());
        assertNotNull(token.getCreatedAt());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));
        assertTrue(token.getExpiresAt().isBefore(LocalDateTime.now().plusHours(25)));

        verify(verificationEmailService).sendVerificationEmail(savedUser, token);

        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("pending_verification", response.getStatus());
        assertEquals(
                "Account created. Check your email to verify your account.",
                response.getMessage()
        );
    }

    @Test
    void registerRejectsPasswordConfirmationMismatch() {
        RegisterRequest request = validRegisterRequest();
        request.setConfirmPassword("DifferentPassword1");

        assertThrows(BadRequestException.class, () -> authService.register(request));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(emailVerificationTokenRepository);
        verifyNoInteractions(verificationEmailService);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = validRegisterRequest();

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailVerificationTokenRepository);
        verifyNoInteractions(verificationEmailService);
    }

    private RegisterRequest validRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("John.Doe@example.com");
        request.setPassword("Password1");
        request.setConfirmPassword("Password1");
        return request;
    }
}