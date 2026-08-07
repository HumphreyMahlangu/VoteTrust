package io.github.humphreymahlangu.votetrust.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.humphreymahlangu.votetrust.support.PostgreSqlTestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AuthControllerIntegrationTest extends PostgreSqlTestContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerLoginAndReadCurrentAccount() throws Exception {
        String registerBody = """
                {
                  "email": "integration.voter@example.com",
                  "password": "VeryStrongPassword1"
                }
                """;

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("integration.voter@example.com"))
                .andExpect(jsonPath("$.role").value("VOTER"))
                .andReturn();

        String registerToken = JsonPath.read(registerResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration.voter@example.com"))
                .andExpect(jsonPath("$.role").value("VOTER"))
                .andExpect(jsonPath("$.enabled").value(true));

        String loginBody = """
                {
                  "email": "INTEGRATION.VOTER@example.com",
                  "password": "VeryStrongPassword1"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("integration.voter@example.com"));
    }

    @Test
    void currentAccountRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws Exception {
        String registerBody = """
                {
                  "email": "duplicate.voter@example.com",
                  "password": "VeryStrongPassword1"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A user account with this email already exists"));
    }

    @Test
    void weakRegistrationPasswordReturnsValidationError() throws Exception {
        String registerBody = """
                {
                  "email": "weak.password@example.com",
                  "password": "lowercaseonly"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.password").value(
                        "must contain at least one uppercase letter, one lowercase letter, and one digit"
                ));
    }
}
