package com.lordkaysudo.aisupportcopilotapi.retrieval.api;

import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import com.lordkaysudo.aisupportcopilotapi.retrieval.service.KnowledgeRetrievalService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/retrieval")
public class RetrievalController {

    private final KnowledgeRetrievalService retrievalService;

    public RetrievalController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam @NotBlank String query,
            @RequestParam(defaultValue = "3") @Min(1) @Max(20) int topK,
            @RequestParam(defaultValue = "0.5") double minScore
    ) {
        List<RetrievedDocument> documents = retrievalService.retrieveByQuery(query, topK, minScore);
        return ResponseEntity.ok(Map.of(
                "query", query,
                "topK", topK,
                "minScore", minScore,
                "count", documents.size(),
                "results", documents
        ));
    }
}
