package com.lordkaysudo.aisupportcopilotapi.copilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "copilot.retrieval")
public record CopilotProperties(
        int topK,
        double minScore,
        int embeddingDimensions
) {
}
