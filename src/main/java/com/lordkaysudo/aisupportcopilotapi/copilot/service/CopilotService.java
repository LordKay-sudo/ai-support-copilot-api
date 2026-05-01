package com.lordkaysudo.aisupportcopilotapi.copilot.service;

import com.lordkaysudo.aisupportcopilotapi.copilot.config.CopilotProperties;
import com.lordkaysudo.aisupportcopilotapi.copilot.model.Citation;
import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerRequest;
import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerResponse;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import com.lordkaysudo.aisupportcopilotapi.retrieval.service.KnowledgeRetrievalService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopilotService {

    private final KnowledgeRetrievalService retrievalService;
    private final CopilotProperties copilotProperties;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    public CopilotService(
            KnowledgeRetrievalService retrievalService,
            CopilotProperties copilotProperties,
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider
    ) {
        this.retrievalService = retrievalService;
        this.copilotProperties = copilotProperties;
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    public CopilotAnswerResponse answer(CopilotAnswerRequest request) {
        List<RetrievedDocument> docs = retrievalService.retrieve(
                request,
                copilotProperties.topK(),
                copilotProperties.minScore()
        );

        String answer = generateAnswer(request, docs);
        double confidence = docs.isEmpty() ? 0.30 : 0.78;
        boolean escalationRequired = docs.isEmpty() || request.customerTier().equalsIgnoreCase("enterprise");

        return new CopilotAnswerResponse(
                answer,
                confidence,
                docs.stream()
                        .map(doc -> new Citation(doc.id(), doc.title(), doc.score()))
                        .toList(),
                List.of(
                        "Confirm customer identity before account actions.",
                        "Run product-specific troubleshooting checklist.",
                        "Escalate when recovery flow fails after verified steps."
                ),
                escalationRequired
        );
    }

    private String generateAnswer(CopilotAnswerRequest request, List<RetrievedDocument> docs) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null || docs.isEmpty()) {
            return "Use the documented recovery workflow for " + request.product()
                    + " and validate identity before performing account changes.";
        }

        String context = docs.stream()
                .map(doc -> "- " + doc.title() + ": " + doc.content())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String prompt = """
                You are a support copilot. Answer using only the provided context.
                If context is insufficient, say what information is missing.

                Ticket: %s
                Customer tier: %s
                Product: %s
                Question: %s

                Context:
                %s
                """.formatted(
                request.ticketId(),
                request.customerTier(),
                request.product(),
                request.question(),
                context
        );

        ChatClient chatClient = builder.build();
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
