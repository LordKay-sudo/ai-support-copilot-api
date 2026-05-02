package com.lordkaysudo.aisupportcopilotapi.copilot.api;

import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerRequest;
import com.lordkaysudo.aisupportcopilotapi.copilot.model.CopilotAnswerResponse;
import com.lordkaysudo.aisupportcopilotapi.copilot.service.CopilotService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/copilot")
public class CopilotController {

    private final CopilotService copilotService;

    public CopilotController(CopilotService copilotService) {
        this.copilotService = copilotService;
    }

    @PostMapping("/answer")
    @RateLimiter(name = "copilotAnswer")
    public ResponseEntity<CopilotAnswerResponse> answer(
            @Valid @RequestBody CopilotAnswerRequest request) {
        return ResponseEntity.ok(copilotService.answer(request));
    }
}
