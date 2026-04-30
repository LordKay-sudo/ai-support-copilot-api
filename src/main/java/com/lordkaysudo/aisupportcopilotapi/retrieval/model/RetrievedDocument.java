package com.lordkaysudo.aisupportcopilotapi.retrieval.model;

public record RetrievedDocument(
        String id,
        String title,
        String content,
        double score
) {
}
