package com.pickleball.controller;

import com.pickleball.dto.MemberDto;
import com.pickleball.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 본인 프로필 조회
     * GET /api/v1/members/me
     */
    @GetMapping("/me")
    public ResponseEntity<MemberDto.ProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(memberService.getProfile(authentication.getName()));
    }

    /**
     * 본인 프로필 수정
     * PUT /api/v1/members/me
     */
    @PutMapping("/me")
    public ResponseEntity<MemberDto.ProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody MemberDto.ProfileUpdateRequest request) {
        return ResponseEntity.ok(memberService.updateProfile(authentication.getName(), request));
    }

    /**
     * 본인 비밀번호 변경
     * PUT /api/v1/members/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody MemberDto.PasswordChangeRequest request) {
        memberService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
