package com.thecoderkushagra.service;


import com.thecoderkushagra.entity.Repository;
import com.thecoderkushagra.entity.SemanticCache;
import com.thecoderkushagra.repository.SemanticCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticCacheService {

    private final SemanticCacheRepository semanticCacheRepository;

    public Optional<String> checkCache(UUID repositoryId, String vectorString) {
        log.info("Checking semantic cache for repository {}", repositoryId);
        
        Optional<SemanticCache> match = semanticCacheRepository.findHighlySimilarCache(repositoryId, vectorString);
        
        if (match.isPresent()) {
            log.info("Semantic cache HIT for repository {}", repositoryId);
            return Optional.of(match.get().getResponseText());
        }
        
        log.info("Semantic cache MISS for repository {}", repositoryId);
        return Optional.empty();
    }

    @Async
    public void saveCache(Repository repository, String queryText, String vectorString, String responseText) {
        try {
            log.info("Saving semantic cache async for repository {}", repository.getId());
            
            SemanticCache cache = SemanticCache.builder()
                    .repository(repository)
                    .queryText(queryText)
                    .responseText(responseText)
                    .queryEmbedding(vectorString)
                    .build();
                    
            semanticCacheRepository.save(cache);
        } catch (Exception e) {
            log.error("Failed to save semantic cache: {}", e.getMessage());
        }
    }
}
