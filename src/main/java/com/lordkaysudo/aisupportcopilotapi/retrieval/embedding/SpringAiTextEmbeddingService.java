package com.lordkaysudo.aisupportcopilotapi.retrieval.embedding;

import com.lordkaysudo.aisupportcopilotapi.copilot.config.CopilotProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class SpringAiTextEmbeddingService implements TextEmbeddingService {

    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final int dimensions;

    public SpringAiTextEmbeddingService(
            ObjectProvider<EmbeddingModel> embeddingModelProvider,
            CopilotProperties copilotProperties
    ) {
        this.embeddingModelProvider = embeddingModelProvider;
        this.dimensions = Math.max(8, copilotProperties.embeddingDimensions());
    }

    @Override
    public float[] embed(String text) {
        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel == null) {
            return deterministicEmbedding(text);
        }
        return embeddingModel.embed(text);
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    private float[] deterministicEmbedding(String text) {
        float[] vector = new float[dimensions];
        String normalized = text == null ? "" : text.toLowerCase();
        for (int i = 0; i < normalized.length(); i++) {
            int bucket = i % dimensions;
            vector[bucket] += normalized.charAt(i);
        }
        normalize(vector);
        return vector;
    }

    private void normalize(float[] vector) {
        double mag = 0.0;
        for (float v : vector) {
            mag += v * v;
        }
        mag = Math.sqrt(mag);
        if (mag == 0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / mag);
        }
    }
}
