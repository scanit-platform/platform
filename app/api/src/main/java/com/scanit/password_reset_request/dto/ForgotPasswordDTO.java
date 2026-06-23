package com.scanit.password_reset_request.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordDTO(
        @NotBlank
        @Email
        String email
) {}
