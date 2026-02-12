package com.pickleball.dto;

import com.pickleball.entity.Court;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PartnerCourtDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String courtName;
        private Short personnelNumber;
        private String courtLevel; // 실내/실외 등
        private String gameTime;
        private String reservClose; // Y/N
        private String courtGugun;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String courtName;
        private Short personnelNumber;
        private String courtLevel;
        private String gameTime;
        private String reservClose;
        private String courtGugun;
        private LocalDateTime createDate;

        public static Response from(Court court) {
            return Response.builder()
                    .id(court.getId())
                    .courtName(court.getCourtName())
                    .personnelNumber(court.getPersonnelNumber())
                    .courtLevel(court.getCourtLevel())
                    .gameTime(court.getGameTime())
                    .reservClose(court.getReservClose())
                    .courtGugun(court.getCourtGugun())
                    .createDate(court.getCreateDate())
                    .build();
        }
    }
}
