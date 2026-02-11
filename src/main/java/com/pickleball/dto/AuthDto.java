package com.pickleball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
}
