package com.pickleball.service;

import com.pickleball.dto.RentalRequestDto;
import com.pickleball.entity.Account;
import com.pickleball.entity.Court;
import com.pickleball.entity.CourtSchedule;
import com.pickleball.entity.Partner;
import com.pickleball.entity.Reservation;
import com.pickleball.exception.BusinessException;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.CourtRepository;
import com.pickleball.repository.CourtScheduleRepository;
import com.pickleball.repository.PartnerRepository;
import com.pickleball.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RentalRequestService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime RENTAL_MIN_START = LocalTime.of(9, 0);

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final CourtScheduleRepository courtScheduleRepository;
    private final AccountRepository accountRepository;
    private final PartnerRepository partnerRepository;

    @Transactional
    public RentalRequestDto.Response createRequest(RentalRequestDto.CreateRequest request, String username) {
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if ("Y".equals(court.getReservClose())) {
            throw new BusinessException(BusinessException.ErrorCode.COURT_CLOSED);
        }

        LocalTime startTime = parseTime(request.getStartTime());
        LocalTime endTime = parseTime(request.getEndTime());

        validateRequestTimes(request.getGameDate(), startTime, endTime);

        String timeSlot = formatTimeSlot(startTime, endTime);

        reservationRepository.findExistingRentalRequest(
                        username,
                        request.getCourtId(),
                        request.getGameDate(),
                        timeSlot,
                        Set.of("PENDING", "APPROVED"))
                .ifPresent(r -> {
                    throw new BusinessException(BusinessException.ErrorCode.ALREADY_RESERVED);
                });

        ensureNoScheduleOverlap(request.getCourtId(), request.getGameDate(), startTime, endTime, true);

        Reservation reservation = Reservation.builder()
                .courtId(request.getCourtId())
                .username(username)
                .cancelYn("N")
                .timeName(request.getTimeName() != null && !request.getTimeName().isBlank() ? request.getTimeName()
                        : "대관")
                .gameDate(request.getGameDate())
                .timeSlot(timeSlot)
                .reservType("1")
                .approvalStatus("PENDING")
                .build();

        reservationRepository.save(reservation);

        return RentalRequestDto.Response.builder()
                .id(reservation.getId())
                .courtId(reservation.getCourtId())
                .username(reservation.getUsername())
                .gameDate(reservation.getGameDate())
                .timeSlot(reservation.getTimeSlot())
                .approvalStatus(reservation.getApprovalStatus())
                .createdAt(reservation.getCreateDate())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RentalRequestDto.Response> getMyRequests(String username) {
        return reservationRepository.findMyRentalRequests(username).stream()
                .map(r -> RentalRequestDto.Response.builder()
                        .id(r.getId())
                        .courtId(r.getCourtId())
                        .username(r.getUsername())
                        .gameDate(r.getGameDate())
                        .timeSlot(r.getTimeSlot())
                        .approvalStatus(r.getApprovalStatus())
                        .createdAt(r.getCreateDate())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RentalRequestDto.PartnerListItem> getPartnerRequests(
            String partnerUsername,
            Long courtId,
            LocalDate date,
            String status) {

        Partner partner = getPartnerByUsername(partnerUsername);
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        List<String> statuses = resolveRentalRequestStatuses(status);

        return reservationRepository.findRentalRequests(courtId, date, statuses).stream()
                .map(r -> RentalRequestDto.PartnerListItem.builder()
                    .id(r.getId())
                        .courtId(r.getCourtId())
                        .username(r.getUsername())
                        .name(r.getAccount() != null ? r.getAccount().getName() : null)
                        .gameDate(r.getGameDate())
                        .timeSlot(r.getTimeSlot())
                        .approvalStatus(r.getApprovalStatus())
                        .createdAt(r.getCreateDate())
                        .build())
                .toList();
    }

    @Transactional
    public RentalRequestDto.Response approve(String partnerUsername, Long requestId) {
        Reservation reservation = reservationRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.RESERVATION_NOT_FOUND));

        if (!"1".equals(reservation.getReservType())) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_REQUEST_STATE);
        }

        if (!"PENDING".equalsIgnoreCase(reservation.getApprovalStatus())) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_REQUEST_STATE);
        }

        Partner partner = getPartnerByUsername(partnerUsername);
        Court court = courtRepository.findById(reservation.getCourtId())
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        LocalTime[] range = parseTimeSlotRange(reservation.getTimeSlot());
        LocalTime startTime = range[0];
        LocalTime endTime = range[1];

        ensureNoScheduleOverlap(reservation.getCourtId(), reservation.getGameDate(), startTime, endTime, true);

        CourtSchedule schedule = CourtSchedule.builder()
                .courtId(reservation.getCourtId())
                .gameDate(reservation.getGameDate())
                .startTime(startTime)
                .endTime(endTime)
                .scheduleType("RENTAL")
                .lockedYn("Y")
                .build();
        courtScheduleRepository.save(schedule);

        reservation.setApprovalStatus("APPROVED");
        reservationRepository.save(reservation);

        return RentalRequestDto.Response.builder()
                .id(reservation.getId())
                .courtId(reservation.getCourtId())
                .username(reservation.getUsername())
                .gameDate(reservation.getGameDate())
                .timeSlot(reservation.getTimeSlot())
                .approvalStatus(reservation.getApprovalStatus())
                .createdAt(reservation.getCreateDate())
                .build();
    }

    @Transactional
    public RentalRequestDto.Response reject(String partnerUsername, Long requestId) {
        Reservation reservation = reservationRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.RESERVATION_NOT_FOUND));

        if (!"1".equals(reservation.getReservType())) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_REQUEST_STATE);
        }

        if (!"PENDING".equalsIgnoreCase(reservation.getApprovalStatus())) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_REQUEST_STATE);
        }

        Partner partner = getPartnerByUsername(partnerUsername);
        Court court = courtRepository.findById(reservation.getCourtId())
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        reservation.setApprovalStatus("REJECTED");
        reservationRepository.save(reservation);

        return RentalRequestDto.Response.builder()
                .id(reservation.getId())
                .courtId(reservation.getCourtId())
                .username(reservation.getUsername())
                .gameDate(reservation.getGameDate())
                .timeSlot(reservation.getTimeSlot())
                .approvalStatus(reservation.getApprovalStatus())
                .createdAt(reservation.getCreateDate())
                .build();
    }

    private Partner getPartnerByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.MEMBER_NOT_FOUND));

        return partnerRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateRequestTimes(LocalDate gameDate, LocalTime startTime, LocalTime endTime) {
        int startMinute = toMinute(startTime);
        int endMinute = toEndMinute(startTime, endTime);

        if (startMinute < toMinute(RENTAL_MIN_START) || startMinute >= endMinute) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }

        LocalDateTime startDateTime = LocalDateTime.of(gameDate, startTime);
        if (LocalDateTime.now().isAfter(startDateTime)) {
            throw new BusinessException(BusinessException.ErrorCode.GAME_TIME_PASSED);
        }
    }

    private void ensureNoScheduleOverlap(Long courtId, LocalDate gameDate, LocalTime startTime, LocalTime endTime,
            boolean allowOpenGameOverlap) {
        int reqStart = toMinute(startTime);
        int reqEnd = toEndMinute(startTime, endTime);

        if (reqStart >= reqEnd) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }

        List<CourtSchedule> schedules = courtScheduleRepository.findByCourtIdAndGameDateOrderByStartTime(courtId,
                gameDate);

        for (CourtSchedule s : schedules) {
            if (allowOpenGameOverlap && isOpenGameSchedule(s)) {
                continue;
            }

            int sStart = toMinute(s.getStartTime());
            int sEnd = toEndMinute(s.getStartTime(), s.getEndTime());
            if (overlaps(reqStart, reqEnd, sStart, sEnd)) {
                throw new BusinessException(BusinessException.ErrorCode.SCHEDULE_OVERLAP);
            }
        }
    }

    private boolean isOpenGameSchedule(CourtSchedule schedule) {
        String scheduleType = schedule.getScheduleType();
        return scheduleType != null && "OPEN_GAME".equalsIgnoreCase(scheduleType.trim());
    }

    private boolean overlaps(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart < bEnd && aEnd > bStart;
    }

    private int toMinute(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private int toEndMinute(LocalTime startTime, LocalTime endTime) {
        int start = toMinute(startTime);
        int end = toMinute(endTime);
        if (endTime.equals(LocalTime.MIDNIGHT) && start > 0) {
            return 1440;
        }
        return end;
    }

    private LocalTime[] parseTimeSlotRange(String timeSlot) {
        if (timeSlot == null) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }

        String[] split = timeSlot.split("~");
        if (split.length != 2) {
            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }

        LocalTime startTime = parseTime(split[0].trim());
        LocalTime endTime = parseTime(split[1].trim());
        return new LocalTime[] { startTime, endTime };
    }

    private String formatTimeSlot(LocalTime startTime, LocalTime endTime) {
        return startTime.format(TIME_FORMAT) + "~" + endTime.format(TIME_FORMAT);
    }

    private LocalTime parseTime(String value) {
        String normalized = value == null ? "" : value.trim();

        try {
            return LocalTime.parse(normalized, TIME_FORMAT);
        } catch (Exception e) {
            if ("24:00".equals(normalized)) {
                return LocalTime.MIDNIGHT;
            }
            throw new BusinessException(BusinessException.ErrorCode.INVALID_TIME_SLOT);
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ALL";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private List<String> resolveRentalRequestStatuses(String status) {
        String normalized = normalizeStatus(status);
        if ("ALL".equals(normalized)) {
            return List.of("PENDING", "APPROVED");
        }
        return List.of(normalized);
    }
}
