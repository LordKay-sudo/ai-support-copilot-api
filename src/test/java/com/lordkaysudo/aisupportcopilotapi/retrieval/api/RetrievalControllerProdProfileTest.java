package com.lordkaysudo.aisupportcopilotapi.retrieval.api;

import com.lordkaysudo.aisupportcopilotapi.support.TestJwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.secret-key=12345678901234567890123456789012")
class RetrievalControllerProdProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotRegisterDebugSearchEndpointWhenDisabledInProd() throws Exception {
        mockMvc.perform(get("/api/retrieval/search")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                        .param("query", "mfa password reset")
                        .param("topK", "3")
                        .param("minScore", "0.2"))
                .andExpect(status().isNotFound());
    }
}
