package com.lordkaysudo.aisupportcopilotapi.retrieval.api;

import com.lordkaysudo.aisupportcopilotapi.support.TestJwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RetrievalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnRetrievalResultsForValidQuery() throws Exception {
        mockMvc.perform(get("/api/retrieval/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .param("query", "mfa password reset")
                        .param("topK", "3")
                        .param("minScore", "0.2"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.query").value("mfa password reset"))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    void shouldReturnBadRequestForMissingQuery() throws Exception {
        mockMvc.perform(get("/api/retrieval/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .param("topK", "3")
                        .param("minScore", "0.2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldReturnBadRequestWhenMinScoreBelowZero() throws Exception {
        mockMvc.perform(get("/api/retrieval/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .param("query", "test")
                        .param("minScore", "-0.01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(containsString("minScore")));
    }

    @Test
    void shouldReturnBadRequestWhenMinScoreAboveOne() throws Exception {
        mockMvc.perform(get("/api/retrieval/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .param("query", "test")
                        .param("minScore", "1.01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(containsString("minScore")));
    }

    @Test
    void shouldAcceptMinScoreAtInclusiveBounds() throws Exception {
        mockMvc.perform(get("/api/retrieval/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .param("query", "mfa password reset")
                        .param("minScore", "0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());

        mockMvc.perform(get("/api/retrieval/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .param("query", "mfa password reset")
                        .param("minScore", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }
}
