package com.thecoderkushagra.service;

import com.thecoderkushagra.dto.request.ChatMessageDto;
import com.thecoderkushagra.dto.request.ChatRequest;
import com.thecoderkushagra.entity.AstChunk;
import com.thecoderkushagra.entity.Repository;
import com.thecoderkushagra.exception.ResourceNotFoundException;
import com.thecoderkushagra.repository.RepositoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final GuardrailService guardrailService;
    private final SemanticCacheService semanticCacheService;
    private final VectorRetrievalService vectorRetrievalService;
    private final EmbeddingModel embeddingModel;
    private final ChatClient.Builder chatClientBuilder;
    private final RepositoryRepository repositoryRepository;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        this.chatClient = chatClientBuilder.build();
    }

    public Flux<String> processChat(UUID repositoryId, ChatRequest request) {
        log.info("Processing chat for repository {}", repositoryId);
        
        // Step 1: Security Guardrail Check
        guardrailService.validateQuery(request.query());

        // Step 2: Single Embedding Generation
        float[] vector = embeddingModel.embed(request.query());
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        String vectorString = sb.toString();

        // Step 3: Cache Check
        Optional<String> cachedResponse = semanticCacheService.checkCache(repositoryId, vectorString);
        if (cachedResponse.isPresent()) {
            return Flux.just(cachedResponse.get());
        }

        // Fetch Repository for cache saving later
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));

        // Step 4: Vector Retrieval
        List<AstChunk> chunks = vectorRetrievalService.retrieveContext(repositoryId, vectorString);
        String joinedContext = chunks.stream()
                .map(AstChunk::getContent)
                .collect(Collectors.joining("\n"));

        // Prompt Engineering & Chat Memory
        List<Message> messages = new ArrayList<>();
        String systemText = "You are an expert AI coding assistant. Answer the user's question using ONLY the provided repository context. If the answer is not in the context, say so. \n\n CONTEXT:\n" + joinedContext;
        messages.add(new SystemMessage(systemText));

        if (request.history() != null) {
            for (ChatMessageDto msg : request.history()) {
                if ("user".equalsIgnoreCase(msg.role())) {
                    messages.add(new UserMessage(msg.content()));
                } else if ("assistant".equalsIgnoreCase(msg.role())) {
                    messages.add(new AssistantMessage(msg.content()));
                }
            }
        }
        
        messages.add(new UserMessage(request.query()));
        Prompt prompt = new Prompt(messages);

        // Execute LLM stream
        log.info("Invoking LLM ChatClient via Streaming");
        StringBuilder responseBuilder = new StringBuilder();
        
        return chatClient.prompt(prompt)
                .stream()
                .content()
                .doOnNext(responseBuilder::append)
                .doOnComplete(() -> semanticCacheService.saveCache(repository, request.query(), vectorString, responseBuilder.toString()));
    }
}
