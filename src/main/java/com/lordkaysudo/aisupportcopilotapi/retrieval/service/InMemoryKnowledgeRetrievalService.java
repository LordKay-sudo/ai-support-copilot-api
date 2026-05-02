package com.lordkaysudo.aisupportcopilotapi.retrieval.service;

import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerRequest;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import com.lordkaysudo.aisupportcopilotapi.retrieval.store.KnowledgeStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InMemoryKnowledgeRetrievalService implements KnowledgeRetrievalService {

    private final KnowledgeStore knowledgeStore;

    public InMemoryKnowledgeRetrievalService(KnowledgeStore knowledgeStore) {
        this.knowledgeStore = knowledgeStore;
    }

    @Override
    public List<RetrievedDocument> retrieve(CopilotAnswerRequest request, int topK, double minScore) {
        String query = request.product() + " " + request.question();
        return retrieveByQuery(query, topK, minScore);
    }

    @Override
    public List<RetrievedDocument> retrieveByQuery(String query, int topK, double minScore) {
        return knowledgeStore.semanticSearch(query, topK, minScore);
    }
}
