package com.lordkaysudo.aisupportcopilotapi.knowledge.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldIngestKnowledgeDocument() throws Exception {
        String requestBody = """
                {
                  "id": "kb-999",
                  "title": "Tenant Reset Workflow",
                  "content": "Reset tenant credentials after ownership verification.",
                  "confidenceScore": 88
                }
                """;

        mockMvc.perform(post("/api/knowledge/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.id").value("kb-999"))
                .andExpect(jsonPath("$.status").value("INGESTED"));
    }
}
