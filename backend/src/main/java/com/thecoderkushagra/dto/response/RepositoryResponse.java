package com.thecoderkushagra.dto.response;

import java.util.UUID;

public record RepositoryResponse(
    UUID id,
    String name,
    String gitUrl,
    String status
) {}
