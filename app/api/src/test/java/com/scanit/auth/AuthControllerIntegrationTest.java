package com.scanit.auth;

import com.scanit.user.model.UserStatus;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterActiveUserWithoutEmailVerification() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("active-register@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("active-register@example.com"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.message").value("Account created. You can sign in now."));

        assertThat(userRepository.findByEmail("active-register@example.com").orElseThrow().getStatus())
                .isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldRejectDuplicateEmailDuringRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("duplicate@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("duplicate@example.com"))
                .andExpect(jsonPath("$.status").value("active"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("duplicate@example.com")))
                .andExpect(status().isConflict());
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
}
