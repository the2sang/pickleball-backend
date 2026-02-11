package com.pickleball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AdminDto {

    /**
     * 사업장 상세 응답 (관리자용 - 전체 필드)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerDetailResponse {
        private Long id;
        private Long accountId;
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
        private long courtCount;
    }

    /**
     * 사업장 수정 요청 (아이디/패스워드 제외)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerUpdateRequest {
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
