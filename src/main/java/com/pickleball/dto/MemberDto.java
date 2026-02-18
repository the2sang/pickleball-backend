package com.pickleball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class MemberDto {

    /**
     * 회원 프로필 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileResponse {
        private Long id;
        private String username;
        private String name;
        private String phoneNumber;
        private String nicName;
        private String email;
        private String sex;
        private String ageRange;
        private String location;
        private String circleName;
        private String gameLevel;
        private String duprPoint;
        private String memberLevel;
        private String registDate;
    }

    /**
     * 회원 프로필 수정 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileUpdateRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String phoneNumber;
        private String nicName;
        private String email;
        private String sex;
        private String ageRange;
        private String location;
        private String circleName;
        private String gameLevel;
        private String duprPoint;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordChangeRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;
        @NotBlank
        private String confirmPassword;
    }
}
