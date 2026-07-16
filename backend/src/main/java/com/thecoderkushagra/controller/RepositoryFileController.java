package com.thecoderkushagra.controller;

import com.thecoderkushagra.dto.ApiResponse;
import com.thecoderkushagra.dto.response.SourceFileResponseDto;
import com.thecoderkushagra.entity.User;
import com.thecoderkushagra.repository.SourceFileRepository;
import com.thecoderkushagra.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/files")
@RequiredArgsConstructor
@Slf4j
public class RepositoryFileController {

    private final RepositoryService repositoryService;
    private final SourceFileRepository sourceFileRepository;

    @GetMapping
    public ApiResponse<List<SourceFileResponseDto>> getSourceFiles(
            @PathVariable UUID repositoryId,
            @AuthenticationPrincipal User user) {
        
        log.info("Fetching source files for repository {} by user {}", repositoryId, user.getEmail());
        
        // Ensure user owns or has access to the repository
        repositoryService.getValidatedRepository(repositoryId, user);
        
        List<SourceFileResponseDto> files = sourceFileRepository.findByRepositoryId(repositoryId)
                .stream()
                .map(file -> new SourceFileResponseDto(file.getId(), file.getFilePath(), file.getContent()))
                .collect(Collectors.toList());
                
        return ApiResponse.<List<SourceFileResponseDto>>builder()
                .success(true)
                .message("Source files retrieved successfully")
                .data(files)
                .build();
    }
}
