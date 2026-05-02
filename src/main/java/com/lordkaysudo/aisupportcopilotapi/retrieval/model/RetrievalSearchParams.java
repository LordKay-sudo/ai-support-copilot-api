package com.lordkaysudo.aisupportcopilotapi.retrieval.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Validated retrieval query parameters (built from request params with defaults, then checked via {@link jakarta.validation.Validator}).
 */
public record RetrievalSearchParams(
        @NotBlank String query,
        @Min(1) @Max(20) int topK,
        @DecimalMin(value = "0.0", inclusive = true)
        @DecimalMax(value = "1.0", inclusive = true)
        double minScore
) {
    public static RetrievalSearchParams fromHttp(String query, Integer topK, Double minScore) {
        int resolvedTopK = topK != null ? topK : 3;
        double resolvedMinScore = minScore != null ? minScore : 0.5;
        return new RetrievalSearchParams(query != null ? query : "", resolvedTopK, resolvedMinScore);
    }
}
