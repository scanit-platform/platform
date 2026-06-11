package com.scanit.auth;

import com.scanit.auth.dto.AuthRequest;
import com.scanit.auth.dto.AuthResponse;
import com.scanit.auth.dto.RegisterRequest;
import com.scanit.exception.InvalidCredentialsException;
import com.scanit.exception.PasswordStrengthException;
import com.scanit.security.JwtService;
import com.scanit.security.UserPrincipal;
import com.scanit.user.dto.UserRequestDto;
import com.scanit.user.dto.UserResponseDto;
import com.scanit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try{
            UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setName(request.getName());
            userRequestDto.setEmail(request.getEmail());
            userRequestDto.setPassword(request.getPassword());

            UserResponseDto user = userService.registerUser(userRequestDto);
            return buildAuthResponse(user.getId(), user.getName(), user.getEmail());
        } catch (PasswordStrengthException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Registration failed: " + ex.getMessage(), ex);
        }
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

    private AuthResponse buildAuthResponse(Long id, String name, String email) {
        String token = jwtService.generateToken(id, email);
        return new AuthResponse(id, name, email, token, jwtService.extractExpiration(token));
    }
}
