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
}
