package com.thecoderkushagra.repository;

import com.thecoderkushagra.entity.SemanticCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SemanticCacheRepository extends JpaRepository<SemanticCache, UUID> {

    @Query(value = """
        SELECT * FROM semantic_cache 
        WHERE repository_id = :repositoryId 
        AND (query_embedding <=> cast(:queryEmbedding AS vector)) <= 0.08
        ORDER BY query_embedding <=> cast(:queryEmbedding AS vector) 
        LIMIT 1
        """, nativeQuery = true)
    Optional<SemanticCache> findHighlySimilarCache(
        @Param("repositoryId") UUID repositoryId, 
        @Param("queryEmbedding") String queryEmbedding
    );
}
