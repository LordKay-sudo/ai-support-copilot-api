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
import java.util.regex.Pattern;

@Service
public class CopilotService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b(?:\\+?\\d{1,3}[\\s.-]?)?(?:\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4})\\b");
    private static final Pattern CARD_PATTERN =
            Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");


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

        String answer = redactSensitiveData(generateAnswer(request, docs));
        double confidence = docs.isEmpty() ? 0.30 : 0.78;
        boolean containsRedactions = answer.contains("[REDACTED_");
        boolean escalationRequired = docs.isEmpty()
                || request.customerTier().equalsIgnoreCase("enterprise")
                || containsRedactions;

        List<String> suggestedActions = List.of(
                "Confirm customer identity before account actions.",
                "Run product-specific troubleshooting checklist.",
                "Escalate when recovery flow fails after verified steps."
        );
        if (containsRedactions) {
            suggestedActions = List.of(
                    "Sensitive data was detected and redacted. Validate details over a secure support channel.",
                    "Confirm customer identity before account actions.",
                    "Escalate when recovery flow fails after verified steps."
            );
        }

        return new CopilotAnswerResponse(
                answer,
                confidence,
                docs.stream()
                        .map(doc -> new Citation(doc.id(), doc.title(), doc.score()))
                        .toList(),
                suggestedActions,
                escalationRequired
        );
    }

    private String generateAnswer(CopilotAnswerRequest request, List<RetrievedDocument> docs) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null || docs.isEmpty()) {
            return "For support issue '" + request.question() + "', use the documented recovery workflow for " + request.product()
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

    private String redactSensitiveData(String answer) {
        String redacted = EMAIL_PATTERN.matcher(answer).replaceAll("[REDACTED_EMAIL]");
        redacted = PHONE_PATTERN.matcher(redacted).replaceAll("[REDACTED_PHONE]");
        return CARD_PATTERN.matcher(redacted).replaceAll("[REDACTED_CARD]");
    }
}
