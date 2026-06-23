package com.scanit.password_reset_request.controller;

import com.scanit.password_reset_request.dto.ForgotPasswordDTO;
import com.scanit.password_reset_request.dto.PasswordResetRequestDTO;
import com.scanit.password_reset_request.service.ResetPasswordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/password-reset-request")
@CrossOrigin
public class PasswordResetController {
    private final ResetPasswordService resetPasswordService;

    public PasswordResetController(ResetPasswordService resetPasswordService) {
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        resetPasswordService.requestResetPassword(dto.email());

        return ResponseEntity.ok(Map.of("message", "If an account matches that email, a secure link will be sent"));
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestParam String token) {
        resetPasswordService.validateToken(token);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetRequestDTO dto) {
        resetPasswordService.resetPassword(dto);
        return ResponseEntity.ok(Map.of("message", "Password successfully reset!"));
    }
}
