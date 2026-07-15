package com.thecoderkushagra.service;

import com.thecoderkushagra.entity.AstChunk;
import com.thecoderkushagra.repository.AstChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final AstChunkRepository astChunkRepository;

    public List<AstChunk> retrieveContext(UUID repositoryId, String vectorString) {
        log.info("Fetching top semantic matches from database for vector.");

        List<AstChunk> initialMatches = astChunkRepository.findSimilarChunks(repositoryId, vectorString, 15);

        List<AstChunk> truncatedMatches = new ArrayList<>();
        int currentLength = 0;
        int budgetLimit = 12000;

        for (AstChunk chunk : initialMatches) {
            int chunkLength = chunk.getContent() != null ? chunk.getContent().length() : 0;
            if (currentLength + chunkLength > budgetLimit) {
                log.info("Context budget reached ({} chars). Truncating remaining matches.", currentLength);
                break;
            }
            truncatedMatches.add(chunk);
            currentLength += chunkLength;
        }

        log.info("Returning {} strictly budgeted semantic matches for retrieval", truncatedMatches.size());
        return truncatedMatches;
    }
}
