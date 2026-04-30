package com.lordkaysudo.aisupportcopilotapi.retrieval.service;

import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerRequest;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import com.lordkaysudo.aisupportcopilotapi.retrieval.store.KnowledgeStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class InMemoryKnowledgeRetrievalService implements KnowledgeRetrievalService {

    private final KnowledgeStore knowledgeStore;

    public InMemoryKnowledgeRetrievalService(KnowledgeStore knowledgeStore) {
        this.knowledgeStore = knowledgeStore;
    }

    @Override
    public List<RetrievedDocument> retrieve(CopilotAnswerRequest request, int topK, double minScore) {
        String question = request.question().toLowerCase(Locale.ROOT);
        String product = request.product().toLowerCase(Locale.ROOT);

        return knowledgeStore.findAll().stream()
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
