package com.lordkaysudo.aisupportcopilotapi.common.api;

import java.util.List;

public record ApiErrorResponse(
        int status,
        String message,
        List<String> errors,
        String requestId
) {
}
