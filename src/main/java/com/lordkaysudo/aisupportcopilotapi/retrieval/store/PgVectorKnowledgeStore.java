package com.lordkaysudo.aisupportcopilotapi.retrieval.store;

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

    public PgVectorKnowledgeStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
                    score DOUBLE PRECISION NOT NULL
                )
                """);
    }

    @Override
    public void upsert(RetrievedDocument document) {
        jdbcTemplate.update("""
                INSERT INTO knowledge_documents (id, title, content, score)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE SET title = EXCLUDED.title,
                              content = EXCLUDED.content,
                              score = EXCLUDED.score
                """,
                document.id(),
                document.title(),
                document.content(),
                document.score());
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
}
