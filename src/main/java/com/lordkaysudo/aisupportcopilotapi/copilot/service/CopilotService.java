package com.lordkaysudo.aisupportcopilotapi.copilot.service;

import com.lordkaysudo.aisupportcopilotapi.copilot.model.Citation;
import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerRequest;
import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopilotService {

    public CopilotAnswerResponse answer(CopilotAnswerRequest request) {
        // Placeholder implementation for Milestone 2.
        // Milestone 3 will replace this with retrieval + model orchestration.
        return new CopilotAnswerResponse(
                "Thanks. I have received your support question and will return a grounded response after retrieval is integrated.",
                0.25,
                List.of(new Citation("bootstrap-doc", "Milestone 2 placeholder response", 0.10)),
                List.of(
                        "Collect any relevant account identifiers from the customer.",
                        "Review known issues for the selected product before responding.",
                        "Escalate if issue impacts production workload."
                ),
                false
        );
    }
}
