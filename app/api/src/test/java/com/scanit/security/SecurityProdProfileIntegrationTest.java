package com.scanit.security;

import com.scanit.auth.repository.EmailVerificationTokenRepository;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:scanit-prod-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.security.jwt.secret=test-signing-secret-key-with-32-bytes",
        "app.security.jwt.expiration=24h",
        "app.base-url=http://localhost:3000",
        "spring.cloud.aws.credentials.access-key=test-access-key",
        "spring.cloud.aws.credentials.secret-key=test-secret-key",
        "spring.cloud.aws.region.static=eu-west-1",
        "spring.cloud.aws.s3.receipts-bucket=test-receipts",
        "spring.cloud.aws.ses.from=test@example.com",
        "app.email.from=test@example.com",
        "app.email.delivery-enabled=false"
})
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class SecurityProdProfileIntegrationTest {
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
    void shouldAllowPublicEndpointsWithoutH2ConsoleInProdProfile() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("prod@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("prod@example.com"))
                .andExpect(jsonPath("$.status").value("pending_verification"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    private String registerPayload(String email) {
        return (
                "{\n"
                        + "  \"firstName\": \"Prod\",\n"
                        + "  \"lastName\": \"User\",\n"
                        + "  \"email\": \"%s\",\n"
                        + "  \"password\": \"password123\",\n"
                        + "  \"confirmPassword\": \"password123\"\n"
                        + "}\n")
                .formatted(email);
    }
}
