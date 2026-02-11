package com.pickleball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class PartnerProfileDto {

    /**
     * 사업장 프로필 응답 (본인 조회용)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String username;
        private String businessPartner;
        private String owner;
        private String phoneNumber;
        private String partnerAddress;
        private String partnerLevel;
        private String partnerEmail;
        private String registDate;
        private String partnerAccount;
        private String partnerBank;
        private String howToPay;
    }

    /**
     * 사업장 프로필 수정 요청 (아이디/패스워드 제외)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
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
}
