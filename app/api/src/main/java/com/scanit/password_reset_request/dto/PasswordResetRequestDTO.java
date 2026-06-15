package com.scanit.password_reset_request.dto;

import com.scanit.password_reset_request.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDTO(
        @NotBlank
        String token,

        @NotBlank
        @ValidPassword
        String password,

        @NotBlank
        String confirmPassword
) {}
