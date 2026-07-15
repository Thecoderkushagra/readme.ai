package com.thecoderkushagra.dto;

public record ParsedChunkDto(
    String filePath,
    String nodeType,
    String content
) {}
