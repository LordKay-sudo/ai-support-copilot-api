package com.lordkaysudo.aisupportcopilotapi.retrieval.embedding;

public interface TextEmbeddingService {

    float[] embed(String text);

    int dimensions();
}
