package com.scanit.auth;

import com.scanit.auth.dto.RegisterRequest;
import com.scanit.auth.dto.RegistrationResponse;
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

        verifyNoInteractions(userRepository, passwordEncoder);
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
        verifyNoInteractions(passwordEncoder);
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

        RegistrationResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("dana.test@example.com");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getEmail()).isEqualTo("dana.test@example.com");
        assertThat(response.getStatus()).isEqualTo("active");
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

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstName()).isEqualTo("Dana");
        assertThat(savedUser.getLastName()).isEqualTo("Test");
        assertThat(savedUser.getName()).isEqualTo("Dana Test");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
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
}
