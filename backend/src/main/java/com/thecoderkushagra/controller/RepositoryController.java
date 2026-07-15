package com.thecoderkushagra.controller;

import com.thecoderkushagra.dto.ApiResponse;
import com.thecoderkushagra.dto.request.ImportRepositoryRequest;
import com.thecoderkushagra.dto.response.RepositoryResponse;
import com.thecoderkushagra.dto.response.RepositoryStatusResponse;
import com.thecoderkushagra.entity.Repository;
import com.thecoderkushagra.enums.RepositoryStatus;
import com.thecoderkushagra.entity.User;
import com.thecoderkushagra.service.RepositoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<RepositoryResponse>> importRepository(
            @Valid @RequestBody ImportRepositoryRequest request,
            @AuthenticationPrincipal User user) {
        Repository repository = repositoryService.importRepository(request.gitUrl(), user);
        RepositoryResponse response = new RepositoryResponse(
                repository.getId(),
                repository.getName(),
                repository.getStatus().name()
        );
        ApiResponse<RepositoryResponse> apiResponse = ApiResponse.<RepositoryResponse>builder()
                .success(true)
                .message("Repository import initiated")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RepositoryResponse>>> getRepositories(@AuthenticationPrincipal User user) {
        List<Repository> repos = repositoryService.getRepositories(user);
        List<RepositoryResponse> response = repos.stream()
                .map(r -> new RepositoryResponse(r.getId(), r.getName(), r.getStatus().name()))
                .toList();
        ApiResponse<List<RepositoryResponse>> apiResponse = ApiResponse.<List<RepositoryResponse>>builder()
                .success(true)
                .message("Repositories retrieved successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RepositoryStatusResponse>> getRepositoryStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        RepositoryStatus status = repositoryService.getRepositoryStatus(id, user);
        RepositoryStatusResponse response = new RepositoryStatusResponse(status.name());
        ApiResponse<RepositoryStatusResponse> apiResponse = ApiResponse.<RepositoryStatusResponse>builder()
                .success(true)
                .message("Repository status retrieved successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRepository(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        repositoryService.deleteRepository(id, user);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .success(true)
                .message("Repository removed")
                .data("Repository removed")
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
