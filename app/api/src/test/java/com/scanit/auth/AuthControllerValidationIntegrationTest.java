package com.scanit.auth;

import com.scanit.auth.repository.EmailVerificationTokenRepository;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerValidationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @BeforeEach
    void cleanDatabase() {
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectPasswordShorterThanEightCharactersAtEndpointLevel() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("short@example.com", "abc123", "abc123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Password must contain at least 8 characters")));
    }

    @Test
    void shouldRejectPasswordWithoutNumberAtEndpointLevel() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("nonumber@example.com", "abcdefgh", "abcdefgh")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Password must contain at least one letter and one number")));
    }

    @Test
    void shouldRejectPasswordWithoutLetterAtEndpointLevel() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("noletter@example.com", "12345678", "12345678")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Password must contain at least one letter and one number")));
    }

    @Test
    void shouldRejectMissingConfirmPasswordAtEndpointLevel() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n"
                                + "  \"firstName\": \"Test\",\n"
                                + "  \"lastName\": \"User\",\n"
                                + "  \"email\": \"missing-confirm@example.com\",\n"
                                + "  \"password\": \"Password1\"\n"
                                + "}\n"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Confirm password cannot be empty")));
    }

    @Test
    void shouldRejectPasswordMismatchAtEndpointLevel() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("mismatch@example.com", "Password1", "Password2")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Password and confirm password do not match.")));
    }

    private String registerPayload(String email, String password, String confirmPassword) {
        return (
                "{\n"
                        + "  \"firstName\": \"Test\",\n"
                        + "  \"lastName\": \"User\",\n"
                        + "  \"email\": \"%s\",\n"
                        + "  \"password\": \"%s\",\n"
                        + "  \"confirmPassword\": \"%s\"\n"
                        + "}\n")
                .formatted(email, password, confirmPassword);
    }
}
