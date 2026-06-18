package com.scanit.auth;

import com.scanit.auth.dto.AuthRequest;
import com.scanit.auth.dto.AuthResponse;
import com.scanit.auth.dto.RegisterRequest;
import com.scanit.auth.dto.RegistrationResponse;
import com.scanit.auth.dto.ResendVerificationRequest;
import com.scanit.security.UserPrincipal;
import com.scanit.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration and authentication API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Register a new user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered in pending verification state"),
                    @ApiResponse(responseCode = "409", description = "User already exists")
            })
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(authService.register(request));
    }

    @GetMapping("/verify-email")
    @SecurityRequirements
    @Operation(summary = "Verify a user's email address",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email verified successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid or expired verification link")
            })
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    @SecurityRequirements
    @Operation(summary = "Resend email verification link",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verification email resent"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            })
    public ResponseEntity<RegistrationResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(authService.resendVerificationEmail(request.getEmail()));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Authenticate user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "403", description = "Account is not verified")
            })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(authService.login(request));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current user loaded"),
                    @ApiResponse(responseCode = "401", description = "Authentication required")
            })
    public ResponseEntity<UserResponseDto> me(
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            UserPrincipal currentUser) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(authService.getCurrentUser(currentUser));
    }
}