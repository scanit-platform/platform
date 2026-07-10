package com.scanit.auth;

import com.scanit.auth.dto.AuthRequest;
import com.scanit.auth.dto.AuthResponse;
import com.scanit.auth.dto.RegisterRequest;
import com.scanit.auth.dto.RegistrationResponse;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        validatePasswordConfirmation(request);

        String normalizedEmail = normalizeEmail(request.getEmail());
        validateEmailUniqueness(normalizedEmail);

        User user = createActiveUser(request, normalizedEmail);
        User savedUser = userRepository.save(user);

        return new RegistrationResponse(
                savedUser.getEmail(),
                "active",
                "Account created. You can sign in now."
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
                    "Account is disabled.",
                    ex
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password", ex);
        }
    }

    public UserResponseDto getCurrentUser(UserPrincipal currentUser) {
        return new UserResponseDto(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getEmail()
        );
    }

    private User createActiveUser(RegisterRequest request, String normalizedEmail) {
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
                UserStatus.ACTIVE,
                LocalDateTime.now()
        );
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
