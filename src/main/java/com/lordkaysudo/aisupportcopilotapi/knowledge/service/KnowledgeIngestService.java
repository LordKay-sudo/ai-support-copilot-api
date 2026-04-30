package com.lordkaysudo.aisupportcopilotapi.knowledge.service;

import com.lordkaysudo.aisupportcopilotapi.knowledge.model.KnowledgeIngestRequest;
import com.lordkaysudo.aisupportcopilotapi.knowledge.model.KnowledgeIngestResponse;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import com.lordkaysudo.aisupportcopilotapi.retrieval.store.KnowledgeStore;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeIngestService {

    private final KnowledgeStore knowledgeStore;

    public KnowledgeIngestService(KnowledgeStore knowledgeStore) {
        this.knowledgeStore = knowledgeStore;
    }

    public KnowledgeIngestResponse ingest(KnowledgeIngestRequest request) {
        double normalizedScore = request.confidenceScore() / 100.0;
        knowledgeStore.upsert(new RetrievedDocument(
                request.id(),
                request.title(),
                request.content(),
                normalizedScore
        ));

        return new KnowledgeIngestResponse(request.id(), "INGESTED");
    }
}
