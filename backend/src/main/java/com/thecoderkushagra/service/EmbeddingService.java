package com.thecoderkushagra.service;

import com.thecoderkushagra.dto.ParsedChunkDto;
import com.thecoderkushagra.entity.AstChunk;
import com.thecoderkushagra.entity.Repository;
import com.thecoderkushagra.repository.AstChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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
        int totalSkipped = 0;
        
        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<ParsedChunkDto> batch = chunks.subList(i, end);
            
            log.info("Processing batch {} to {} out of {}", i, end, chunks.size());
            
            List<AstChunk> entities = new ArrayList<>();
            for (ParsedChunkDto dto : batch) {
                try {
                    float[] vector = embeddingModel.embed(dto.content());
                    if (entities.isEmpty()) {
                        log.info("Vector length is: {}", vector.length);
                    }
                    String vectorString = Arrays.toString(vector);
                    AstChunk chunk = AstChunk.builder()
                            .repository(repository)
                            .filePath(dto.filePath())
                            .nodeType(dto.nodeType())
                            .content(dto.content())
                            .embedding(vectorString)
                            .build();
                    entities.add(chunk);
                } catch (Exception e) {
                    totalSkipped++;
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                    if (errorMsg.contains("429") || errorMsg.contains("quota") || errorMsg.contains("rate")) {
                        log.warn("Rate limited while embedding chunk from {}. Pausing 5s. Skipped so far: {}", dto.filePath(), totalSkipped);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("Interrupted during rate-limit backoff");
                            break;
                        }
                    } else {
                        log.warn("Failed to embed chunk from {}: {}. Skipping.", dto.filePath(), errorMsg);
                    }
                }
            }
            if (!entities.isEmpty()) {
                astChunkRepository.saveAll(entities);
                totalProcessed += entities.size();
            }
        }
        
        log.info("Vectorization complete for repository {}. Processed: {}, Skipped: {}", repository.getName(), totalProcessed, totalSkipped);
    }
}
