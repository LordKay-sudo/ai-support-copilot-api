package com.lordkaysudo.aisupportcopilotapi.knowledge.api;

import com.lordkaysudo.aisupportcopilotapi.knowledge.model.KnowledgeIngestRequest;
import com.lordkaysudo.aisupportcopilotapi.knowledge.model.KnowledgeIngestResponse;
import com.lordkaysudo.aisupportcopilotapi.knowledge.service.KnowledgeIngestService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestService knowledgeIngestService;

    public KnowledgeController(KnowledgeIngestService knowledgeIngestService) {
        this.knowledgeIngestService = knowledgeIngestService;
    }

    @PostMapping("/ingest")
    @RateLimiter(name = "knowledgeIngest")
    public ResponseEntity<KnowledgeIngestResponse> ingest(
            @Valid @RequestBody KnowledgeIngestRequest request) {
        return ResponseEntity.ok(knowledgeIngestService.ingest(request));
    }
}
