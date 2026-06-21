package com.scanit.auth;

import com.scanit.auth.model.EmailVerificationToken;
import com.scanit.auth.repository.EmailVerificationTokenRepository;
import com.scanit.user.model.User;
import com.scanit.user.model.UserStatus;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectDuplicateEmailDuringRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("duplicate@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("duplicate@example.com"))
                .andExpect(jsonPath("$.status").value("pending_verification"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("duplicate@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidVerificationToken() throws Exception {
        mockMvc.perform(get("/auth/verify-email")
                        .param("token", "not-a-real-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectExpiredVerificationToken() throws Exception {
        User user = savePendingUser("expired@example.com");

        EmailVerificationToken expiredToken = new EmailVerificationToken(
                null,
                "expired-token",
                user,
                LocalDateTime.now().minusMinutes(1),
                null,
                LocalDateTime.now().minusHours(25)
        );

        emailVerificationTokenRepository.save(expiredToken);

        mockMvc.perform(get("/auth/verify-email")
                        .param("token", "expired-token"))
                .andExpect(status().isBadRequest());

        User unchangedUser = userRepository.findByEmail("expired@example.com")
                .orElseThrow();

        assertThat(unchangedUser.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
    }

    @Test
    void shouldResendVerificationEmailAndCreateNewToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("resend@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending_verification"));

        User user = userRepository.findByEmail("resend@example.com")
                .orElseThrow();

        EmailVerificationToken originalToken = emailVerificationTokenRepository.findByUser(user)
                .orElseThrow();

        String originalTokenValue = originalToken.getToken();

        mockMvc.perform(post("/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resendPayload("resend@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("resend@example.com"))
                .andExpect(jsonPath("$.status").value("pending_verification"));

        EmailVerificationToken updatedToken = emailVerificationTokenRepository.findByUser(user)
                .orElseThrow();

        assertThat(updatedToken.getToken()).isNotBlank();
        assertThat(updatedToken.getToken()).isNotEqualTo(originalTokenValue);
        assertThat(updatedToken.getUsedAt()).isNull();
        assertThat(updatedToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void shouldReturnAlreadyActiveMessageWhenResendingVerificationForActiveUser() throws Exception {
        saveActiveUser("active@example.com");

        mockMvc.perform(post("/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resendPayload("active@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("active@example.com"))
                .andExpect(jsonPath("$.status").value("active"));
    }

    private User savePendingUser(String email) {
        User user = new User(
                null,
                "Test User",
                "Test",
                "User",
                email,
                passwordEncoder.encode(PASSWORD),
                UserStatus.PENDING_VERIFICATION,
                LocalDateTime.now()
        );

        return userRepository.save(user);
    }

    private User saveActiveUser(String email) {
        User user = new User(
                null,
                "Active User",
                "Active",
                "User",
                email,
                passwordEncoder.encode(PASSWORD),
                UserStatus.ACTIVE,
                LocalDateTime.now()
        );

        return userRepository.save(user);
    }

    private String registerPayload(String email) {
        return (
                "{\n"
                        + "  \"firstName\": \"Test\",\n"
                        + "  \"lastName\": \"User\",\n"
                        + "  \"email\": \"%s\",\n"
                        + "  \"password\": \"%s\",\n"
                        + "  \"confirmPassword\": \"%s\"\n"
                        + "}\n")
                .formatted(email, PASSWORD, PASSWORD);
    }

    private String resendPayload(String email) {
        return (
                "{\n"
                        + "  \"email\": \"%s\"\n"
                        + "}\n")
                .formatted(email);
    }
}