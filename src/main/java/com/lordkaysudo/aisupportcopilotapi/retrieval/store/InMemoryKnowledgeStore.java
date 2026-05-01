package com.lordkaysudo.aisupportcopilotapi.retrieval.store;

import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(
        name = "copilot.retrieval.store-type",
        havingValue = "in-memory",
        matchIfMissing = true
)
public class InMemoryKnowledgeStore implements KnowledgeStore {

    private final ConcurrentHashMap<String, RetrievedDocument> docsById = new ConcurrentHashMap<>();

    public InMemoryKnowledgeStore() {
        upsert(new RetrievedDocument(
                "kb-241",
                "MFA Recovery Procedure",
                "Use backup factors for MFA recovery before attempting a password reset.",
                0.91
        ));
        upsert(new RetrievedDocument(
                "kb-108",
                "Password Reset Escalation Policy",
                "Escalate to L2 support when enterprise customers cannot complete verified recovery.",
                0.84
        ));
        upsert(new RetrievedDocument(
                "kb-302",
                "Identity-Service Known Issues",
                "A known issue can block reset flow after MFA enrollment on outdated tenant configs.",
                0.73
        ));
    }

    @Override
    public void upsert(RetrievedDocument document) {
        docsById.put(document.id(), document);
    }

    @Override
    public List<RetrievedDocument> findAll() {
        return docsById.values().stream().toList();
    }

    @Override
    public List<RetrievedDocument> semanticSearch(String queryText, int topK, double minScore) {
        String query = queryText == null ? "" : queryText.toLowerCase(Locale.ROOT);

        return docsById.values().stream()
                .map(doc -> new RetrievedDocument(
                        doc.id(),
                        doc.title(),
                        doc.content(),
                        lexicalScore(query, doc)
                ))
                .filter(doc -> doc.score() >= minScore)
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topK)
                .toList();
    }

    private double lexicalScore(String queryText, RetrievedDocument doc) {
        String title = doc.title().toLowerCase(Locale.ROOT);
        String content = doc.content().toLowerCase(Locale.ROOT);
        long matches = Stream.of("mfa", "password", "reset", "recovery", "escalate", "identity")
                .filter(keyword -> queryText.contains(keyword) && (title.contains(keyword) || content.contains(keyword)))
                .count();

        if (matches == 0) {
            return 0.0;
        }
        return Math.min(1.0, 0.4 + (matches * 0.15));
    }
}
