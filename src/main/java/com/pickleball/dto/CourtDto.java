package com.pickleball.dto;

import com.pickleball.entity.Court;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class CourtDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long partnerId;
        private String courtName;
        private Short personnelNumber;
        private String courtLevel;
        private String reservClose;
        private String gameTime;
        private LocalDate gameDate;

        public static Response from(Court court) {
            return Response.builder()
                    .id(court.getId())
                    .partnerId(court.getPartnerId())
                    .courtName(court.getCourtName())
                    .personnelNumber(court.getPersonnelNumber())
                    .courtLevel(court.getCourtLevel())
                    .reservClose(court.getReservClose())
                    .gameTime(court.getGameTime())
                    .gameDate(court.getGameDate())
                    .build();
        }
    }
}
