package com.lordkaysudo.aisupportcopilotapi.knowledge.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeIngestRequest(
        @NotBlank @Size(max = 64) String id,
        @NotBlank @Size(max = 256) String title,
        @NotBlank @Size(max = 10000) String content,
        @Min(0) @Max(100) int confidenceScore
) {
}
