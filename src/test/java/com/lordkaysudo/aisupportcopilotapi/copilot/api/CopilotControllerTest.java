package com.lordkaysudo.aisupportcopilotapi.copilot.api;

import com.lordkaysudo.aisupportcopilotapi.support.TestJwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CopilotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAnswerForValidRequest() throws Exception {
        String requestBody = """
                {
                  "ticketId": "TCK-10021",
                  "question": "Customer cannot reset password after MFA setup",
                  "customerTier": "enterprise",
                  "product": "identity-service",
                  "language": "en"
                }
                """;

        mockMvc.perform(post("/api/copilot/answer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.answer").isNotEmpty())
                .andExpect(jsonPath("$.confidence").isNumber())
                .andExpect(jsonPath("$.sources").isArray())
                .andExpect(jsonPath("$.suggestedActions").isArray())
                .andExpect(jsonPath("$.escalationRequired").isBoolean());
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        String requestBody = """
                {
                  "ticketId": "",
                  "question": "",
                  "customerTier": "enterprise",
                  "product": "identity-service",
                  "language": "en"
                }
                """;

        mockMvc.perform(post("/api/copilot/answer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void shouldRedactSensitiveDataAndEscalate() throws Exception {
        String requestBody = """
                {
                  "ticketId": "TCK-PII-1",
                  "question": "Customer email john.doe@example.com phone 555-123-4567 card 4111 1111 1111 1111 cannot access account",
                  "customerTier": "standard",
                  "product": "unknown-product",
                  "language": "en"
                }
                """;

        mockMvc.perform(post("/api/copilot/answer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("[REDACTED_EMAIL]")))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("[REDACTED_PHONE]")))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("[REDACTED_CARD]")))
                .andExpect(jsonPath("$.escalationRequired").value(true));
    }
}
