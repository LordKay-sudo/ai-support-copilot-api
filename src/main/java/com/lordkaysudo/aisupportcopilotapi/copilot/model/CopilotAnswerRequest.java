package com.lordkaysudo.aisupportcopilotapi.copilot.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CopilotAnswerRequest(
        @NotBlank @Size(max = 64) String ticketId,
        @NotBlank @Size(max = 4000) String question,
        @NotBlank @Size(max = 64) String customerTier,
        @NotBlank @Size(max = 128) String product,
        @NotBlank @Size(max = 8) String language
) {
}
