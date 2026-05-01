package com.lordkaysudo.aisupportcopilotapi.retrieval.store;

import com.lordkaysudo.aisupportcopilotapi.retrieval.embedding.TextEmbeddingService;
import com.lordkaysudo.aisupportcopilotapi.retrieval.model.RetrievedDocument;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "copilot.retrieval.store-type", havingValue = "pgvector")
public class PgVectorKnowledgeStore implements KnowledgeStore {

    private static final Logger log = LoggerFactory.getLogger(PgVectorKnowledgeStore.class);
    private final JdbcTemplate jdbcTemplate;
    private final TextEmbeddingService embeddingService;

    public PgVectorKnowledgeStore(
            JdbcTemplate jdbcTemplate,
            TextEmbeddingService embeddingService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingService = embeddingService;
    }

    @PostConstruct
    void initializeSchema() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        } catch (Exception ex) {
            // Keep startup resilient for environments without pgvector preinstalled.
            log.warn("Could not ensure pgvector extension is available: {}", ex.getMessage());
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS knowledge_documents (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    score DOUBLE PRECISION NOT NULL,
                    embedding vector(%d) NOT NULL
                )
                """.formatted(embeddingService.dimensions()));

        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_knowledge_documents_embedding
                ON knowledge_documents
                USING hnsw (embedding vector_cosine_ops)
                """);
    }

    @Override
    public void upsert(RetrievedDocument document) {
        String textToEmbed = document.title() + " " + document.content();
        String vectorLiteral = toVectorLiteral(embeddingService.embed(textToEmbed));

        jdbcTemplate.update("""
                INSERT INTO knowledge_documents (id, title, content, score, embedding)
                VALUES (?, ?, ?, ?, ?::vector)
                ON CONFLICT (id)
                DO UPDATE SET title = EXCLUDED.title,
                              content = EXCLUDED.content,
                              score = EXCLUDED.score,
                              embedding = EXCLUDED.embedding
                """,
                document.id(),
                document.title(),
                document.content(),
                document.score(),
                vectorLiteral);
    }

    @Override
    public List<RetrievedDocument> findAll() {
        return jdbcTemplate.query("""
                        SELECT id, title, content, score
                        FROM knowledge_documents
                        """,
                (rs, rowNum) -> new RetrievedDocument(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDouble("score")
                ));
    }

    @Override
    public List<RetrievedDocument> semanticSearch(String queryText, int topK, double minScore) {
        String queryVector = toVectorLiteral(embeddingService.embed(queryText));

        return jdbcTemplate.query("""
                        SELECT id, title, content, (1 - (embedding <=> ?::vector)) AS similarity
                        FROM knowledge_documents
                        WHERE (1 - (embedding <=> ?::vector)) >= ?
                        ORDER BY embedding <=> ?::vector
                        LIMIT ?
                        """,
                (rs, rowNum) -> new RetrievedDocument(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDouble("similarity")
                ),
                queryVector, queryVector, minScore, queryVector, topK
        );
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
