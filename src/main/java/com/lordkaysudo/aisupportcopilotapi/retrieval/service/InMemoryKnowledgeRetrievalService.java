package com.lordkaysudo.aisupportcopilotapi.retrieval.service;

import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerRequest;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class InMemoryKnowledgeRetrievalService implements KnowledgeRetrievalService {

    private static final List<RetrievedDocument> KNOWLEDGE_BASE = List.of(
            new RetrievedDocument(
                    "kb-241",
                    "MFA Recovery Procedure",
                    "Use backup factors for MFA recovery before attempting a password reset.",
                    0.91
            ),
            new RetrievedDocument(
                    "kb-108",
                    "Password Reset Escalation Policy",
                    "Escalate to L2 support when enterprise customers cannot complete verified recovery.",
                    0.84
            ),
            new RetrievedDocument(
                    "kb-302",
                    "Identity-Service Known Issues",
                    "A known issue can block reset flow after MFA enrollment on outdated tenant configs.",
                    0.73
            )
    );

    @Override
    public List<RetrievedDocument> retrieve(CopilotAnswerRequest request, int topK, double minScore) {
        String question = request.question().toLowerCase(Locale.ROOT);
        String product = request.product().toLowerCase(Locale.ROOT);

        return KNOWLEDGE_BASE.stream()
                .filter(doc -> doc.score() >= minScore)
                .filter(doc -> containsAny(question, doc) || containsAny(product, doc))
                .limit(topK)
                .toList();
    }

    private boolean containsAny(String text, RetrievedDocument doc) {
        String title = doc.title().toLowerCase(Locale.ROOT);
        String content = doc.content().toLowerCase(Locale.ROOT);
        return Stream.of("mfa", "password", "reset", "recovery", "escalate", "identity")
                .anyMatch(keyword -> text.contains(keyword) && (title.contains(keyword) || content.contains(keyword)));
    }
}
