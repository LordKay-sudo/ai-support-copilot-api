package com.lordkaysudo.aisupportcopilotapi.common.api;

import java.util.List;

public record ApiErrorResponse(
        String message,
        List<String> errors
) {
}
