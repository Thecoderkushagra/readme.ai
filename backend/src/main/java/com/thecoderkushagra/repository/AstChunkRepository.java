package com.thecoderkushagra.repository;

import com.thecoderkushagra.entity.AstChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AstChunkRepository extends JpaRepository<AstChunk, UUID> {

    @Query(value = """
        SELECT * FROM ast_chunks 
        WHERE repository_id = :repositoryId 
        ORDER BY embedding <=> cast(:queryEmbedding AS vector) 
        LIMIT :limit
        """, nativeQuery = true)
    List<AstChunk> findSimilarChunks(
        @Param("repositoryId") UUID repositoryId, 
        @Param("queryEmbedding") String queryEmbedding, 
        @Param("limit") int limit
    );
}
