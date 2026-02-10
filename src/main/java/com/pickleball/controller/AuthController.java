package com.pickleball.controller;

import com.pickleball.dto.AuthDto;
import com.pickleball.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 - JWT 토큰 발급
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.TokenResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 회원가입 + 자동 로그인
     * POST /api/v1/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthDto.TokenResponse> signup(
            @Valid @RequestBody AuthDto.SignupRequest request) {
        return ResponseEntity.status(201).body(authService.signup(request));
    }
}
