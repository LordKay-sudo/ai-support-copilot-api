package com.lordkaysudo.aisupportcopilotapi.retrieval.store;

import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
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
}
