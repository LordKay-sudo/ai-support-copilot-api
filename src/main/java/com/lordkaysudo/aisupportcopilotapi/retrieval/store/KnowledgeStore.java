package com.lordkaysudo.aisupportcopilotapi.retrieval.store;

import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;

import java.util.List;

public interface KnowledgeStore {

    void upsert(RetrievedDocument document);

    List<RetrievedDocument> findAll();

    List<RetrievedDocument> semanticSearch(String queryText, int topK, double minScore);
}
