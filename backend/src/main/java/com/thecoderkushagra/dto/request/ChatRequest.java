package com.thecoderkushagra.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ChatRequest(
    @NotBlank(message = "Query cannot be blank")
    String query,
    List<ChatMessageDto> history
) {}
