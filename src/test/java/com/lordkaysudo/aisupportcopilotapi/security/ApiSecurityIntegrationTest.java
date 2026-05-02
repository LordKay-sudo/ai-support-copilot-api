package com.lordkaysudo.aisupportcopilotapi.security;

import com.lordkaysudo.aisupportcopilotapi.support.TestJwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthIsPermittedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void copilotAnswerRequiresAuthentication() throws Exception {
        String body = """
                {
                  "ticketId": "T-1",
                  "question": "How do I reset MFA?",
                  "customerTier": "standard",
                  "product": "identity-service",
                  "language": "en"
                }
                """;
        mockMvc.perform(post("/api/copilot/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void knowledgeIngestForbiddenForAgentRole() throws Exception {
        String body = """
                {
                  "id": "kb-x",
                  "title": "T",
                  "content": "C",
                  "confidenceScore": 50
                }
                """;
        mockMvc.perform(post("/api/knowledge/ingest")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }
}
