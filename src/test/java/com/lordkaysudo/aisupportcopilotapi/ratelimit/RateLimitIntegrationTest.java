package com.lordkaysudo.aisupportcopilotapi.ratelimit;

import com.lordkaysudo.aisupportcopilotapi.support.TestJwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "resilience4j.ratelimiter.instances.copilotAnswer.limit-for-period=1",
        "resilience4j.ratelimiter.instances.copilotAnswer.limit-refresh-period=60s"
})
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void secondCopilotAnswerWithinWindowIsRejectedWith429() throws Exception {
        String body = """
                {
                  "ticketId": "TCK-RL-1",
                  "question": "Customer cannot reset password after MFA setup",
                  "customerTier": "enterprise",
                  "product": "identity-service",
                  "language": "en"
                }
                """;

        mockMvc.perform(post("/api/copilot/answer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/copilot/answer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Too many requests"));
    }
}
