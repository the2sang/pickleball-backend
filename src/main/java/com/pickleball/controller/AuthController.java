package com.pickleball.controller;

import com.pickleball.dto.AuthDto;
import com.pickleball.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 ID 중복 체크
     * GET /api/v1/auth/username/check?username=...
     */
    @GetMapping("/username/check")
    public ResponseEntity<AuthDto.UsernameCheckResponse> checkUsername(
            @Valid @ModelAttribute AuthDto.UsernameCheckRequest request) {
        return ResponseEntity.ok(authService.checkUsername(request));
    }

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
     * 일반 회원가입 + 자동 로그인
     * POST /api/v1/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthDto.TokenResponse> signup(
            @Valid @RequestBody AuthDto.SignupRequest request) {
        return ResponseEntity.status(201).body(authService.signup(request));
    }

    /**
     * 사업장 회원가입 + 자동 로그인
     * POST /api/v1/auth/signup/partner
     */
    @PostMapping("/signup/partner")
    public ResponseEntity<AuthDto.TokenResponse> signupPartner(
            @Valid @RequestBody AuthDto.PartnerSignupRequest request) {
        return ResponseEntity.status(201).body(authService.signupPartner(request));
    }

    /**
     * 이메일로 아이디 찾기
     * POST /api/v1/auth/username/recover
     */
    @PostMapping("/username/recover")
    public ResponseEntity<AuthDto.MessageResponse> recoverUsername(
            @Valid @RequestBody AuthDto.UsernameRecoverRequest request) {
        return ResponseEntity.ok(authService.recoverUsername(request));
    }

    /**
     * 이메일로 임시 비밀번호 발급
     * POST /api/v1/auth/password/reset
     */
    @PostMapping("/password/reset")
    public ResponseEntity<AuthDto.MessageResponse> resetPassword(
            @Valid @RequestBody AuthDto.PasswordResetRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
