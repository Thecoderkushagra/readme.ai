package com.thecoderkushagra.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ImportRepositoryRequest(
    @NotBlank(message = "Git URL is required")
    String gitUrl
) {}
