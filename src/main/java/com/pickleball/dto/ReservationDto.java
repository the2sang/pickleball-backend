package com.pickleball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationDto {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "코트 ID를 입력해주세요")
        private Long courtId;
        @NotNull(message = "게임 날짜를 입력해주세요")
        private LocalDate gameDate;
        @NotBlank(message = "시간대를 입력해주세요")
        private String timeSlot;
        private String teamName;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long courtId;
        private String username;
        private LocalDate gameDate;
        private String timeSlot;
        private String cancelYn;
        private String courtName;
        private String partnerName;
        private LocalDateTime createDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SlotInfo {
        private String timeSlot;
        private int reservedCount;
        private int capacity;
        private String status; // AVAILABLE, FULL, CLOSED
        private String scheduleType; // OPEN_GAME, RENTAL
        private List<PlayerInfo> players;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PlayerInfo {
        private String name;
        private String nicName;
        private String gameLevel;
        private String duprPoint;
        private String sex;
        private LocalDateTime reservedAt;
        private int orderNumber; // 예약 순서 (1, 2, 3, ...)
        private boolean isWaiting; // 대기 회원 여부
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourtSlotResponse {
        private Long courtId;
        private String courtName;
        private String courtLevel;
        private int personnelNumber;
        private List<SlotInfo> slots;
    }
}
