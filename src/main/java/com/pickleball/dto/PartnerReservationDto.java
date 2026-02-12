package com.pickleball.dto;

import com.pickleball.entity.CourtSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PartnerReservationDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingRequest {
        private Long courtId;
        private LocalDate date;
        private List<TimeSlotSetting> timeSlots;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotSetting {
        private String startTime; // "HH:mm"
        private String endTime; // "HH:mm"
        private String type; // "OPEN_GAME" or "RENTAL"
    }

    @Getter
    @Builder
    public static class SettingResponse {
        private Long courtId;
        private LocalDate date;
        private List<TimeSlotResponse> timeSlots;

        public static SettingResponse from(Long courtId, LocalDate date, List<CourtSchedule> schedules) {
            return SettingResponse.builder()
                    .courtId(courtId)
                    .date(date)
                    .timeSlots(schedules.stream()
                            .map(TimeSlotResponse::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TimeSlotResponse {
        private String startTime;
        private String endTime;
        private String type;

        public static TimeSlotResponse from(CourtSchedule schedule) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return TimeSlotResponse.builder()
                    .startTime(schedule.getStartTime().format(formatter))
                    .endTime(schedule.getEndTime().format(formatter))
                    .type(schedule.getScheduleType())
                    .build();
        }
    }
}
