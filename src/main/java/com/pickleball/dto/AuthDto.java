package com.pickleball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ============ Auth DTOs ============

public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "아이디를 입력해주세요")
        private String username;
        @NotBlank(message = "비밀번호를 입력해주세요")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignupRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
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

        @NotNull
        private Boolean agreeService;

        @NotNull
        private Boolean agreePrivacy;

        private Boolean agreeMarketing;

        private Boolean agreeAll;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerSignupRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        @NotBlank
        private String businessPartner;
        @NotBlank
        private String owner;
        @NotBlank
        private String phoneNumber;
        @NotBlank
        private String partnerAddress;
        @NotBlank
        private String partnerEmail;
        private String partnerAccount;
        private String partnerBank;
        private String howToPay;

        @NotNull
        private Boolean agreeService;

        @NotNull
        private Boolean agreePrivacy;

        private Boolean agreeMarketing;

        private Boolean agreeAll;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsernameCheckRequest {
        @NotBlank(message = "아이디를 입력해주세요")
        @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문/숫자/언더스코어(_)만 사용할 수 있습니다")
        private String username;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsernameCheckResponse {
        private boolean available;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private String username;
        private String name;
        private String accountType;
        private List<String> roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsernameRecoverRequest {
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordResetRequest {
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickAccountResponse {
        private String username;
        private String name;
        private String accountType;
    }
}
