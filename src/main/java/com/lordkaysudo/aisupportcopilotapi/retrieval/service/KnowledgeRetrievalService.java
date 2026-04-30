package com.lordkaysudo.aisupportcopilotapi.retrieval.service;

import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerRequest;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;

import java.util.List;

public interface KnowledgeRetrievalService {

    List<RetrievedDocument> retrieve(CopilotAnswerRequest request, int topK, double minScore);
}
