package com.scanit.password_reset_request.service;

import com.scanit.password_reset_request.dto.PasswordResetRequestDTO;

public interface ResetPasswordService {
    void requestResetPassword(String email);

    void resetPassword(PasswordResetRequestDTO dto);

    void validateToken(String token);
}
