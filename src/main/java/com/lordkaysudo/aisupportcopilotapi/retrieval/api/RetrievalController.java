package com.lordkaysudo.aisupportcopilotapi.retrieval.api;

import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievalSearchParams;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import com.lordkaysudo.aisupportcopilotapi.retrieval.service.KnowledgeRetrievalService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/retrieval")
@ConditionalOnProperty(
        prefix = "copilot.retrieval",
        name = "debug-search-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RetrievalController {

    private final KnowledgeRetrievalService retrievalService;
    private final Validator validator;

    public RetrievalController(KnowledgeRetrievalService retrievalService, Validator validator) {
        this.retrievalService = retrievalService;
        this.validator = validator;
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer topK,
            @RequestParam(required = false) Double minScore
    ) {
        RetrievalSearchParams params = RetrievalSearchParams.fromHttp(query, topK, minScore);
        Set<ConstraintViolation<RetrievalSearchParams>> violations = validator.validate(params);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        List<RetrievedDocument> documents = retrievalService.retrieveByQuery(
                params.query(),
                params.topK(),
                params.minScore()
        );
        return ResponseEntity.ok(Map.of(
                "query", params.query(),
                "topK", params.topK(),
                "minScore", params.minScore(),
                "count", documents.size(),
                "results", documents
        ));
    }
}
