package com.thecoderkushagra.dto.response;

import java.util.UUID;

public record SourceFileResponseDto(
        UUID id,
        String filePath,
        String content
) {}
