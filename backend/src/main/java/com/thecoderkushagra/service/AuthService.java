package com.thecoderkushagra.service;

import com.thecoderkushagra.dto.request.LoginRequest;
import com.thecoderkushagra.dto.request.RefreshTokenRequest;
import com.thecoderkushagra.dto.request.SignupRequest;
import com.thecoderkushagra.dto.response.AuthResponse;
import com.thecoderkushagra.dto.response.SignupResponse;
import com.thecoderkushagra.entity.User;
import com.thecoderkushagra.exception.EmailAlreadyExistsException;
import com.thecoderkushagra.exception.InvalidCredentialsException;
import com.thecoderkushagra.exception.InvalidTokenException;
import com.thecoderkushagra.repository.UserRepository;
import com.thecoderkushagra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        User savedUser = userRepository.save(user);

        return new SignupResponse(savedUser.getId(), "User registered successfully");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidTokenException("Refresh token is expired");
        }

        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid refresh token signature or format");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found for this token"));

        if (!jwtService.isTokenValid(refreshToken, user.getEmail())) {
            throw new InvalidTokenException("Refresh token is invalid");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}
