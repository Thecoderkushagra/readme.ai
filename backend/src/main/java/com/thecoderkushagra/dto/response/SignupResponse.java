package com.thecoderkushagra.dto.response;

import java.util.UUID;

public record SignupResponse(
    UUID userId,
    String message
) {}
