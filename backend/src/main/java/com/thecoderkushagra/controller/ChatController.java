package com.thecoderkushagra.controller;

import com.thecoderkushagra.dto.ApiResponse;
import com.thecoderkushagra.dto.request.ChatRequest;
import com.thecoderkushagra.entity.User;
import com.thecoderkushagra.exception.ResourceNotFoundException;
import com.thecoderkushagra.service.RepositoryService;
import com.thecoderkushagra.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final RepositoryService repositoryService;

    @PostMapping(value = "/{repositoryId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @PathVariable UUID repositoryId,
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User user) {

        // Ensure user owns or has access to the repository
        repositoryService.getValidatedRepository(repositoryId, user);

        return chatService.processChat(repositoryId, request);
    }
}
