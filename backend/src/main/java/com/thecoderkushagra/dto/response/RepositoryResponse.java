package com.thecoderkushagra.dto.response;

import java.util.UUID;

public record RepositoryResponse(
    UUID repositoryId,
    String name,
    String status
) {}
