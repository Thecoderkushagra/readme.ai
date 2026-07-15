package com.thecoderkushagra.service;

import com.thecoderkushagra.entity.Repository;
import com.thecoderkushagra.enums.RepositoryStatus;
import com.thecoderkushagra.entity.User;
import com.thecoderkushagra.exception.ResourceNotFoundException;
import com.thecoderkushagra.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Path;
import com.thecoderkushagra.dto.ParsedChunkDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final GitService gitService;
    private final AstParsingService astParsingService;
    private final EmbeddingService embeddingService;

    @Autowired
    @Lazy
    private RepositoryService self;

    @Transactional
    public Repository importRepository(String gitUrl, User user) {
        log.info("Importing repository for user: {} with URL: {}", user.getEmail(), gitUrl);

        Optional<Repository> existing = repositoryRepository.findByGitUrl(gitUrl);

        if (existing.isPresent()) {
            // Global cache hit — link user to existing repository
            Repository repository = existing.get();
            log.info("Repository already exists globally (id: {}). Linking user: {}", repository.getId(), user.getEmail());
            repository.getUsers().add(user);
            Repository saved = repositoryRepository.save(repository);
            self.processRepositoryAsync(saved);
            return saved;
        }

        // New repository — create, link user, and set initial status
        String name = extractRepositoryName(gitUrl);
        Repository repository = Repository.builder()
                .gitUrl(gitUrl)
                .name(name)
                .status(RepositoryStatus.CLONING)
                .build();
        repository.getUsers().add(user);

        Repository saved = repositoryRepository.save(repository);
        self.processRepositoryAsync(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Repository> getRepositories(User user) {
        log.info("Fetching repositories for user: {}", user.getEmail());
        return repositoryRepository.findByUsersId(user.getId());
    }

    @Transactional(readOnly = true)
    public RepositoryStatus getRepositoryStatus(UUID id, User user) {
        log.info("Fetching status for repository: {} for user: {}", id, user.getEmail());
        Repository repository = repositoryRepository.findByIdAndUsersId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found or unauthorized"));
        return repository.getStatus();
    }

    @Transactional
    public void deleteRepository(UUID id, User user) {
        log.info("Removing repository link: {} for user: {}", id, user.getEmail());
        Repository repository = repositoryRepository.findByIdAndUsersId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found or unauthorized"));

        repository.getUsers().remove(user);

        if (repository.getUsers().isEmpty()) {
            // No users left — delete the repository entirely
            log.info("No remaining users for repository: {}. Deleting entirely.", id);
            repositoryRepository.delete(repository);
        } else {
            // Other users still reference this repository — just unlink
            log.info("Other users still linked to repository: {}. Saving updated association.", id);
            repositoryRepository.save(repository);
        }
    }

    private String extractRepositoryName(String gitUrl) {
        if (gitUrl == null || gitUrl.isBlank()) {
            throw new IllegalArgumentException("Git URL cannot be empty");
        }

        String url = gitUrl.trim();

        // Remove trailing slashes
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        // Remove .git suffix if present
        if (url.endsWith(".git")) {
            url = url.substring(0, url.length() - 4);
        }

        // Get last segment after / or :
        int lastSlash = url.lastIndexOf('/');
        int lastColon = url.lastIndexOf(':');
        int lastIndex = Math.max(lastSlash, lastColon);

        if (lastIndex != -1) {
            return url.substring(lastIndex + 1);
        }
        return url;
    }

    @Async
    public void processRepositoryAsync(Repository repository) {
        Path tempDirectory = null;
        try {
            repository.setStatus(RepositoryStatus.PARSING);
            repositoryRepository.save(repository);

            tempDirectory = gitService.cloneRepository(repository.getGitUrl());
            List<ParsedChunkDto> chunks = astParsingService.parseRepository(tempDirectory);
            embeddingService.vectorizeAndSave(repository, chunks);

            repository.setStatus(RepositoryStatus.COMPLETED);
            repositoryRepository.save(repository);
        } catch (Exception e) {
            log.error("Failed to process repository {}: {}", repository.getGitUrl(), e.getMessage());
            repository.setStatus(RepositoryStatus.FAILED);
            repositoryRepository.save(repository);
        } finally {
            if (tempDirectory != null) {
                gitService.cleanupDirectory(tempDirectory);
            }
        }
    }
}
