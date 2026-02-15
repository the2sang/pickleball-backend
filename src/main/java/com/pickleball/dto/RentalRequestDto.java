package com.pickleball.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RentalRequestDto {

    @Data
    public static class CreateRequest {
        @NotNull
        private Long courtId;

        @NotNull
        private LocalDate gameDate;

        @NotNull
        private String startTime;

        @NotNull
        private String endTime;

        private String timeName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long courtId;
        private String username;
        private LocalDate gameDate;
        private String timeSlot;
        private String approvalStatus;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerListItem {
        private Long id;
        private Long courtId;
        private String username;
        private String name;
        private LocalDate gameDate;
        private String timeSlot;
        private String approvalStatus;
        private LocalDateTime createdAt;
    }

    @Data
    public static class RejectRequest {
        private String reason;
    }
}
