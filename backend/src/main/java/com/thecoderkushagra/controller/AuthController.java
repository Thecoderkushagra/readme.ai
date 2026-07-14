package com.thecoderkushagra.controller;

import com.thecoderkushagra.dto.ApiResponse;
import com.thecoderkushagra.dto.request.LoginRequest;
import com.thecoderkushagra.dto.request.RefreshTokenRequest;
import com.thecoderkushagra.dto.request.SignupRequest;
import com.thecoderkushagra.dto.response.AuthResponse;
import com.thecoderkushagra.dto.response.SignupResponse;
import com.thecoderkushagra.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        ApiResponse<SignupResponse> apiResponse = ApiResponse.<SignupResponse>builder()
                .success(true)
                .message("Signup successful")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refresh successful")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
