package com.scanit.password_reset_request.repository;

import com.scanit.password_reset_request.model.PasswordResetRequest;
import com.scanit.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResetPasswordRepository extends JpaRepository<PasswordResetRequest, Long> {
    Optional<PasswordResetRequest> findByToken(String token);
    List<PasswordResetRequest> findByUser(User user);
}
