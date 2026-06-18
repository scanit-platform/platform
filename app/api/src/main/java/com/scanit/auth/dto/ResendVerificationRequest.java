package com.scanit.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email address")
    private String email;
}