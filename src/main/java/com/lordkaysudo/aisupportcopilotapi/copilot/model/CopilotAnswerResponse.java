package com.lordkaysudo.aisupportcopilotapi.copilot.model;

import java.util.List;

public record CopilotAnswerResponse(
        String answer,
        double confidence,
        List<Citation> sources,
        List<String> suggestedActions,
        boolean escalationRequired
) {
}
