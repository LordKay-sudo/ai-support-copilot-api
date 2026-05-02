# ADR-001: Retrieval strategy, API security, and deployment shape

## Status

Accepted

## Context

The service combines Spring-managed HTTP APIs, pluggable knowledge retrieval (in-memory vs PostgreSQL pgvector), and optional Spring AI model calls. Production deployments need clear boundaries for authentication, which surfaces are exposed publicly, and how retrieval quality degrades when the vector store or model is unavailable.

## Decision

1. **Retrieval**  
   - Use a single `KnowledgeStore` abstraction with `in-memory` for fast local development and `pgvector` for cosine similarity search over persisted embeddings.  
   - Embedding generation uses Spring AI when an `EmbeddingModel` bean exists; otherwise a deterministic local embedding keeps pgvector paths testable without external API keys.

2. **Security**  
   - Use Spring Security OAuth2 Resource Server with **HS256** symmetric JWTs for this service (simple ops for a reference deployment).  
   - Map JWT claim `roles` to Spring authorities (`ROLE_AGENT`, `ROLE_ADMIN`).  
   - **AGENT** may call copilot and retrieval debug endpoints; **ADMIN** may ingest knowledge.  
   - Anonymous access is limited to health, selected Actuator endpoints, and OpenAPI/Swagger assets.  
   - Symmetric signing is acceptable for an internal API gateway pattern; for multi-service federation, migrate to OIDC with asymmetric keys and central issuance.

3. **Operational toggles**  
   - `copilot.retrieval.debug-search-enabled` gates registration of the retrieval debug HTTP controller so production defaults stay minimal even when JWTs are valid.

4. **Deployment**  
   - Container image runs the Spring Boot fat JAR.  
   - Docker Compose runs PostgreSQL (pgvector) plus the API with JDBC and JWT secrets supplied via environment variables.

## Consequences

- Callers must obtain JWTs out-of-band (gateway, IdP, or dev tooling); the API does not expose a token minting endpoint by design.  
- HS256 requires secret distribution discipline; rotation implies coordinated config updates.  
- pgvector mode assumes the database schema is created at startup by the store implementation (idempotent DDL).
