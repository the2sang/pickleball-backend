package com.pickleball.service;

import com.pickleball.dto.PartnerReservationDto;
import com.pickleball.entity.Account;
import com.pickleball.entity.Court;
import com.pickleball.entity.CourtSchedule;
import com.pickleball.entity.Partner;
import com.pickleball.exception.BusinessException;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.CourtRepository;
import com.pickleball.repository.CourtScheduleRepository;
import com.pickleball.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerReservationService {

    private final CourtScheduleRepository courtScheduleRepository;
    private final CourtRepository courtRepository;
    private final PartnerRepository partnerRepository;
    private final AccountRepository accountRepository;

    private Partner getPartnerByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.MEMBER_NOT_FOUND));

        return partnerRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.MEMBER_NOT_FOUND));
    }

    public PartnerReservationDto.SettingResponse getSettings(String username, Long courtId, LocalDate date) {
        Partner partner = getPartnerByUsername(username);
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        List<CourtSchedule> schedules = courtScheduleRepository.findByCourtIdAndGameDateOrderByStartTime(courtId, date);
        return PartnerReservationDto.SettingResponse.from(court.getId(), date, schedules);
    }

    @Transactional
    public void saveSettings(String username, PartnerReservationDto.SettingRequest request) {
        Partner partner = getPartnerByUsername(username);
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        validateNoOverlap(request.getTimeSlots());

        // 기존 설정 삭제 (해당 날짜의 모든 스케줄) -> 또는 업데이트 로직
        // 여기서는 간단하게 해당 날짜의 스케줄을 삭제하고 새로 등록하는 방식 사용
        courtScheduleRepository.deleteByCourtIdAndGameDate(request.getCourtId(), request.getDate());
        courtScheduleRepository.flush();

        for (PartnerReservationDto.TimeSlotSetting slot : request.getTimeSlots()) {
            CourtSchedule schedule = CourtSchedule.builder()
                    .courtId(request.getCourtId())
                    .gameDate(request.getDate())
                    .startTime(LocalTime.parse(slot.getStartTime()))
                    .endTime(LocalTime.parse(slot.getEndTime()))
                    .scheduleType(slot.getType())
                    .build();
            courtScheduleRepository.save(schedule);
        }
    }

    private void validateNoOverlap(List<PartnerReservationDto.TimeSlotSetting> timeSlots) {
        if (timeSlots == null || timeSlots.size() < 2) {
            if (timeSlots == null || timeSlots.isEmpty()) {
                return;
            }

            validateSlotBoundary(timeSlots.get(0));
            return;
        }

        List<TimeSlotRange> sorted = timeSlots.stream()
                .map(this::toRange)
                .sorted((a, b) -> Integer.compare(a.startMinute, b.startMinute))
                .toList();

        for (int i = 0; i < sorted.size() - 1; i++) {
            if (sorted.get(i).endMinute > sorted.get(i + 1).startMinute) {
                throw new BusinessException(BusinessException.ErrorCode.SCHEDULE_OVERLAP);
            }
        }
    }

    private void validateSlotBoundary(PartnerReservationDto.TimeSlotSetting slot) {
        toRange(slot);
    }

    private TimeSlotRange toRange(PartnerReservationDto.TimeSlotSetting slot) {
        if (slot == null || slot.getStartTime() == null || slot.getEndTime() == null) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }

        LocalTime startTime = parseTime(slot.getStartTime());
        LocalTime endTime = parseTime(slot.getEndTime());
        int startMinute = toMinute(startTime);
        int endMinute = isMidnight(slot.getEndTime()) ? 1440 : toMinute(endTime);

        if (startMinute >= endMinute) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }

        return new TimeSlotRange(startMinute, endMinute);
    }

    private int toMinute(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private boolean isMidnight(String time) {
        return "00:00".equals(normalizeTime(time));
    }

    private String normalizeTime(String time) {
        return time.trim();
    }

    private LocalTime parseTime(String value) {
        String normalized = value.trim();

        try {
            return LocalTime.parse(normalized);
        } catch (DateTimeParseException e) {
            if ("24:00".equals(normalized)) {
                return LocalTime.MIDNIGHT;
            }

            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }
    }

    private static class TimeSlotRange {
        private final int startMinute;
        private final int endMinute;

        private TimeSlotRange(int startMinute, int endMinute) {
            this.startMinute = startMinute;
            this.endMinute = endMinute;
        }
    }
}
