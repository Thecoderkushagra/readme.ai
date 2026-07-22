package com.thecoderkushagra.service;

import com.thecoderkushagra.entity.Repository;
import com.thecoderkushagra.enums.RepositoryStatus;
import com.thecoderkushagra.entity.SourceFile;
import com.thecoderkushagra.entity.User;
import com.thecoderkushagra.exception.ResourceNotFoundException;
import com.thecoderkushagra.repository.RepositoryRepository;
import com.thecoderkushagra.repository.SourceFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Stream;
import com.thecoderkushagra.dto.ParsedChunkDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final GitService gitService;
    private final AstParsingService astParsingService;
    private final EmbeddingService embeddingService;
    private final SourceFileRepository sourceFileRepository;

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
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    self.processRepositoryAsync(saved.getId());
                }
            });
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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                self.processRepositoryAsync(saved.getId());
            }
        });
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
        Repository repository = getValidatedRepository(id, user);
        return repository.getStatus();
    }

    @Transactional(readOnly = true)
    public Repository getValidatedRepository(UUID repositoryId, User user) {
        return repositoryRepository.findByIdAndUsersId(repositoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found or unauthorized"));
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

    @Transactional
    public void updateRepositoryStatus(UUID repositoryId, RepositoryStatus status) {
        repositoryRepository.findById(repositoryId).ifPresent(repo -> {
            repo.setStatus(status);
            repositoryRepository.save(repo);
        });
    }

    @Async
    public void processRepositoryAsync(UUID repositoryId) {
        Path tempDirectory = null;
        String gitUrl = "unknown";
        try {
            Repository repository = repositoryRepository.findById(repositoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));
            gitUrl = repository.getGitUrl();

            // Phase 1: Clone
            self.updateRepositoryStatus(repositoryId, RepositoryStatus.CLONING);
            tempDirectory = gitService.cloneRepository(gitUrl);

            // Phase 2: Parse AST
            self.updateRepositoryStatus(repositoryId, RepositoryStatus.PARSING);
            List<ParsedChunkDto> chunks = astParsingService.parseRepository(tempDirectory);

            // Phase 3: Vectorize
            self.updateRepositoryStatus(repositoryId, RepositoryStatus.VECTORIZING);
            embeddingService.vectorizeAndSave(repository, chunks);

            // Phase 3.5: Read and save full source files for high-fidelity code viewer
            List<SourceFile> sourceFiles = new ArrayList<>();
            final Path finalTempDirectory = tempDirectory;
            try (Stream<Path> paths = Files.walk(finalTempDirectory)) {
                paths.filter(Files::isRegularFile)
                     .filter(astParsingService::isValidFile)
                     .forEach(path -> {
                         try {
                             String relativePath = finalTempDirectory.relativize(path).toString();
                             String content = readFileContentSafe(path);
                             sourceFiles.add(SourceFile.builder()
                                     .repository(repository)
                                     .filePath(relativePath)
                                     .content(content)
                                     .build());
                         } catch (Exception e) {
                             log.warn("Failed to read source file: {} - {}", path, e.getMessage());
                         }
                     });
            }
            if (!sourceFiles.isEmpty()) {
                sourceFileRepository.saveAll(sourceFiles);
            }

            self.updateRepositoryStatus(repositoryId, RepositoryStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Failed to process repository {}: {}", gitUrl, e.getMessage());
            self.updateRepositoryStatus(repositoryId, RepositoryStatus.FAILED);
        } finally {
            if (tempDirectory != null) {
                gitService.cleanupDirectory(tempDirectory);
            }
        }
    }

    /**
     * Reads file content with encoding fallback. Tries UTF-8 first,
     * falls back to ISO-8859-1 on MalformedInputException.
     */
    private String readFileContentSafe(Path filePath) throws IOException {
        String content;
        try {
            content = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            content = Files.readString(filePath, StandardCharsets.ISO_8859_1);
        }
        return content.replace("\0", "");
    }
}
