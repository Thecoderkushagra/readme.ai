package com.thecoderkushagra.service;

import com.pgvector.PGvector;
import com.thecoderkushagra.dto.ParsedChunkDto;
import com.thecoderkushagra.entity.AstChunk;
import com.thecoderkushagra.entity.Repository;
import com.thecoderkushagra.repository.AstChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final AstChunkRepository astChunkRepository;

    public void vectorizeAndSave(Repository repository, List<ParsedChunkDto> chunks) {
        log.info("Starting vectorization for repository: {}. Total chunks: {}", repository.getName(), chunks.size());
        
        int batchSize = 50;
        int totalProcessed = 0;
        
        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<ParsedChunkDto> batch = chunks.subList(i, end);
            
            log.info("Processing batch {} to {} out of {}", i, end, chunks.size());
            
            List<AstChunk> entities = new ArrayList<>();
            for (ParsedChunkDto dto : batch) {
                try {
                    float[] vector = embeddingModel.embed(dto.content());
                    AstChunk chunk = AstChunk.builder()
                            .repository(repository)
                            .filePath(dto.filePath())
                            .nodeType(dto.nodeType())
                            .content(dto.content())
                            .embedding(new PGvector(vector))
                            .build();
                    entities.add(chunk);
                } catch (Exception e) {
                    log.error("Failed to generate embedding for file: {}. Error: {}", dto.filePath(), e.getMessage());
                    // Decide if we should fail the entire batch/repository or just skip the chunk.
                    // Usually we throw or skip. We will throw to let the orchestrator handle it.
                    throw new RuntimeException("Embedding failed for chunk: " + dto.filePath(), e);
                }
            }
            astChunkRepository.saveAll(entities);
            totalProcessed += entities.size();
        }
        
        log.info("Successfully vectorized and saved {} chunks for repository {}", totalProcessed, repository.getName());
    }
}
