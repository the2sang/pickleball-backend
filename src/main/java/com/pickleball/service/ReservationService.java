package com.pickleball.service;

import com.pickleball.dto.ReservationDto;
import com.pickleball.entity.*;
import com.pickleball.exception.BusinessException;
import com.pickleball.exception.BusinessException.ErrorCode;
import com.pickleball.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

        private final ReservationRepository reservationRepository;
        private final CourtRepository courtRepository;
        private final MemberSuspensionRepository suspensionRepository;
        private final RejectVoteRepository rejectVoteRepository;
        private final AccountRepository accountRepository;
        private final CourtScheduleRepository courtScheduleRepository;

        /**
         * 코트별 시간대 예약 현황 조회
         */
    @Transactional(readOnly = true)
    public ReservationDto.CourtSlotResponse getCourtSlots(Long courtId, LocalDate gameDate) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURT_NOT_FOUND));

                List<CourtSchedule> schedules = courtScheduleRepository
                                .findByCourtIdAndGameDateOrderByStartTime(courtId, gameDate);

        List<ReservationRepositoryCustom.ActivePlayerRow> allRows = reservationRepository
                .findActivePlayerRows(courtId, gameDate);

                List<ReservationDto.SlotInfo> slots = schedules.stream()
                                .map(schedule -> {
                                        String timeSlot = formatTimeSlot(schedule.getStartTime(),
                                                        schedule.getEndTime());

                                        List<ReservationRepositoryCustom.ActivePlayerRow> rows = allRows.stream()
                                                .filter(row -> isSlotMatch(row.timeSlot(), schedule))
                                                .toList();

                                        int count = rows.size();
                                        int capacity = court.getPersonnelNumber() != null ? court.getPersonnelNumber()
                                                        : 6;
                                        boolean isFull = count >= capacity;

                                        // Check if closed by partner setting (Rentals might be treated differently?)
                                        // For OPEN_GAME, it's bookable. For RENTAL, maybe it shows as "Rental Only" or
                                        // similar?
                                        // For now, treat RENTAL as bookable or just display type.
                                        // Existing logic had "reservClose".

                                        String status = isFull ? "FULL" : "AVAILABLE";

                                        if ("Y".equals(court.getReservClose())) {
                                                status = "CLOSED";
                                        }

                                        // If using CourtSchedule, we can also check schedule.getScheduleType()
                                        // e.g. if RENTAL, maybe status is specific?
                                        // For now, adhere to existing logic but use dynamic slots.

                                        // Build player list with order and waiting status
                                        List<ReservationDto.PlayerInfo> players = new ArrayList<>();
                                        for (int i = 0; i < rows.size(); i++) {
                                                ReservationRepositoryCustom.ActivePlayerRow row = rows.get(i);
                                                players.add(ReservationDto.PlayerInfo.builder()
                                                                .reservationId(row.reservationId())
                                                                .username(row.username())
                                                                .name(row.name())
                                                                .nicName(row.nicName())
                                                                .gameLevel(row.gameLevel())
                                                                .duprPoint(row.duprPoint())
                                                                .sex(row.sex())
                                                                .reservedAt(row.reservedAt())
                                                                .orderNumber(i + 1)
                                                                .isWaiting(i >= capacity)
                                                                .build());
                                        }

                                        return ReservationDto.SlotInfo.builder()
                                                        .timeSlot(timeSlot)
                                                        .reservedCount(count)
                                                        .capacity(capacity)
                                                        .status(status)
                                                        .scheduleType(schedule.getScheduleType())
                                                        .players(players)
                                                        .build();
                                })
                                .toList();

                return ReservationDto.CourtSlotResponse.builder()
                                .courtId(court.getId())
                                .courtName(court.getCourtName())
                                .courtLevel(court.getCourtLevel())
                                .personnelNumber(court.getPersonnelNumber() != null ? court.getPersonnelNumber() : 6)
                                .slots(slots)
                                .build();
        }

        private String formatTimeSlot(LocalTime start, LocalTime end) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                return start.format(formatter) + "~" + end.format(formatter);
        }

        /**
         * 특정 시간대 예약자 상세 조회
         */
        @Transactional(readOnly = true)
        public List<ReservationDto.PlayerInfo> getSlotPlayers(
                        Long courtId, LocalDate gameDate, String timeSlot) {
                Court court = courtRepository.findById(courtId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.COURT_NOT_FOUND));
                int capacity = court.getPersonnelNumber() != null ? court.getPersonnelNumber() : 6;

                List<String> timeSlotCandidates = buildTimeSlotCandidates(timeSlot);
                List<ReservationRepositoryCustom.ActivePlayerRow> rows = List.of();

                for (String candidate : timeSlotCandidates) {
                        rows = reservationRepository.findActivePlayerRows(courtId, gameDate, candidate);
                        if (!rows.isEmpty()) {
                                break;
                        }
                }

                List<ReservationDto.PlayerInfo> players = new ArrayList<>();
                for (int i = 0; i < rows.size(); i++) {
                        ReservationRepositoryCustom.ActivePlayerRow row = rows.get(i);
                        players.add(ReservationDto.PlayerInfo.builder()
                                        .reservationId(row.reservationId())
                                        .username(row.username())
                                        .name(row.name())
                                        .nicName(row.nicName())
                                        .gameLevel(row.gameLevel())
                                        .duprPoint(row.duprPoint())
                                        .sex(row.sex())
                                        .reservedAt(row.reservedAt())
                                        .orderNumber(i + 1)
                                        .isWaiting(i >= capacity)
                                        .build());
                }

                return players;
        }

        /**
         * 예약 신청 (핵심 비즈니스 로직)
         */
        @Transactional
         public ReservationDto.Response createReservation(
                         ReservationDto.CreateRequest request, String username) {

                // 1. 코트 유효성 확인
                Court court = courtRepository.findById(request.getCourtId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.COURT_NOT_FOUND));

                if ("Y".equals(court.getReservClose())) {
                        throw new BusinessException(ErrorCode.COURT_CLOSED);
                }

                // 2. 회원 정지 상태 확인
                suspensionRepository.findActiveSuspension(username, court.getPartnerId())
                                .ifPresent(s -> {
                                        throw new BusinessException(ErrorCode.MEMBER_SUSPENDED);
                                });

                // 2.5. 현재 시간이 지난 시간대인지 확인
                LocalDateTime gameStart = parseGameStart(request.getGameDate(), request.getTimeSlot());
                if (LocalDateTime.now().isAfter(gameStart)) {
                        throw new BusinessException(ErrorCode.GAME_TIME_PASSED);
                }

                Account account = accountRepository.findByUsername(username)
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
                boolean isGeneralMember = "MEMBER".equalsIgnoreCase(
                                account.getAccountType() != null ? account.getAccountType().trim() : "");

                LocalTime[] requestedSlot = parseTimeSlotRange(request.getTimeSlot());
                CourtSchedule schedule = courtScheduleRepository
                                .findByCourtIdAndGameDateAndStartTimeAndEndTime(
                                                request.getCourtId(),
                                                request.getGameDate(),
                                                requestedSlot[0],
                                                requestedSlot[1])
                                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TIME_SLOT));

                if (isGeneralMember && "RENTAL".equalsIgnoreCase(
                                schedule.getScheduleType() != null ? schedule.getScheduleType().trim() : "")) {
                        throw new BusinessException(ErrorCode.RENTAL_NOT_ALLOWED);
                }

                // 3. 중복 예약 확인
                reservationRepository.findByUsernameAndCourtIdAndGameDateAndTimeSlotAndCancelYnNotAndApprovalStatus(
                                username, request.getCourtId(), request.getGameDate(),
                                request.getTimeSlot(), "Y", "APPROVED")
                                .ifPresent(r -> {
                                        throw new BusinessException(ErrorCode.ALREADY_RESERVED);
                                });

                // 4. 정원 확인 (비관적 잠금)
                int capacity = court.getPersonnelNumber() != null ? court.getPersonnelNumber() : 6;
                int currentCount = reservationRepository.countWithLock(
                                request.getCourtId(), request.getGameDate(), request.getTimeSlot());

                if (currentCount >= capacity) {
                        throw new BusinessException(ErrorCode.COURT_FULL);
                }

                // 5. 거부 투표 확인
                int rejectCount = rejectVoteRepository.countRejects(
                                request.getCourtId(), request.getTimeSlot(),
                                request.getGameDate(), username);
                int totalVoters = currentCount; // 기존 예약자 수
                if (totalVoters > 0 && rejectCount > totalVoters / 2) {
                        throw new BusinessException(ErrorCode.VOTE_REJECTED);
                }

                // 6. 예약 등록
                Reservation reservation = Reservation.builder()
                                .courtId(request.getCourtId())
                                .username(username)
                                .cancelYn("N")
                                .timeName(request.getTeamName() != null ? request.getTeamName() : "일반예약")
                                .gameDate(request.getGameDate())
                                .timeSlot(request.getTimeSlot())
                                .reservType("0")
                                .approvalStatus("APPROVED")
                                .build();
                reservationRepository.save(reservation);

                log.info("예약 생성 완료 - user: {}, court: {}, date: {}, slot: {}",
                                username, request.getCourtId(), request.getGameDate(), request.getTimeSlot());

                return ReservationDto.Response.builder()
                                .id(reservation.getId())
                                .courtId(reservation.getCourtId())
                                .username(reservation.getUsername())
                                .gameDate(reservation.getGameDate())
                                .timeSlot(reservation.getTimeSlot())
                                .cancelYn(reservation.getCancelYn())
                                .courtName(court.getCourtName())
                                .createDate(reservation.getCreateDate())
                                .build();
        }

        /**
         * 예약 취소
         */
        @Transactional
        public void cancelReservation(Long reservationId, String username) {
                Reservation reservation = reservationRepository.findById(reservationId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

                // 본인 확인
                if (!reservation.getUsername().equals(username)) {
                        throw new BusinessException(ErrorCode.NOT_OWNER);
                }

                // 게임 개시 2시간 전 체크
                LocalDateTime gameStart = parseGameStart(
                                reservation.getGameDate(), reservation.getTimeSlot());
                LocalDateTime deadline = gameStart.minusHours(2);

                if (LocalDateTime.now().isAfter(deadline)) {
                        throw new BusinessException(ErrorCode.CANCEL_DEADLINE_PASSED);
                }

                reservation.setCancelYn("Y");
                reservationRepository.save(reservation);

                log.info("예약 취소 완료 - id: {}, user: {}", reservationId, username);
        }

        /**
         * 내 예약 목록 조회
         */
        @Transactional(readOnly = true)
        public List<ReservationDto.Response> getMyReservations(String username) {
                return reservationRepository.findMyReservations(username).stream()
                                .map(r -> ReservationDto.Response.builder()
                                                .id(r.getId())
                                                .courtId(r.getCourtId())
                                                .username(r.getUsername())
                                                .gameDate(r.getGameDate())
                                                .timeSlot(r.getTimeSlot())
                                                .cancelYn(r.getCancelYn())
                                                .courtName(r.getCourt() != null ? r.getCourt().getCourtName() : null)
                                                .createDate(r.getCreateDate())
                                                .build())
                                .toList();
        }

        private LocalDateTime parseGameStart(LocalDate gameDate, String timeSlot) {
                // timeSlot: "08:00~10:00" -> 시작시간 추출
                String startTime = timeSlot.split("~")[0].trim();
                return LocalDateTime.of(gameDate,
                                parseTime(startTime));
        }

        private boolean isSlotMatch(String sourceTimeSlot, CourtSchedule schedule) {
                if (sourceTimeSlot == null || schedule == null) {
                        return false;
                }

                String[] split = sourceTimeSlot.split("~");
                if (split.length != 2) {
                        return false;
                }

                int sourceStart = parseSlotMinute(split[0], false);
                int sourceEnd = parseSlotMinute(split[1], true);
                int scheduleStart = parseSlotMinute(
                                schedule.getStartTime(),
                                false);
                int scheduleEnd = parseSlotMinute(
                                schedule.getEndTime(),
                                true);

                if (sourceStart < 0 || sourceEnd < 0 || scheduleStart < 0 || scheduleEnd < 0) {
                        return false;
                }

                return sourceStart == scheduleStart && sourceEnd == scheduleEnd;
        }

        private int parseSlotMinute(LocalTime time, boolean isEndTime) {
                if (time == null) {
                        return -1;
                }
                if (time.getHour() == 0 && time.getMinute() == 0 && isEndTime) {
                        return 1440;
                }
                return time.getHour() * 60 + time.getMinute();
        }

        private int parseSlotMinute(String raw, boolean isEndTime) {
                if (raw == null) {
                        return -1;
                }
                String normalized = raw.trim();
                if (normalized.isEmpty()) {
                        return -1;
                }
                if ("24:00".equals(normalized)) {
                        return 1440;
                }
                if ("00:00".equals(normalized)) {
                        return isEndTime ? 1440 : 0;
                }
                try {
                        LocalTime time = parseTime(normalized);
                        return time.getHour() * 60 + time.getMinute();
                } catch (Exception e) {
                        return -1;
                }
        }

        private String normalizeTimeSlotForQuery(String timeSlot) {
                if (timeSlot == null) {
                        return "";
                }

                String normalized = String.valueOf(timeSlot).trim();
                String[] split = normalized.split("~");
                if (split.length != 2) {
                        return normalized.replace(" ", "");
                }

                int start = parseSlotMinute(split[0], false);
                int end = parseSlotMinute(split[1], true);
                if (start < 0 || end < 0) {
                        return normalized.replace(" ", "");
                }

                return formatSlotMinute(start) + "~" + formatSlotMinute(end);
        }

        private List<String> buildTimeSlotCandidates(String timeSlot) {
                Set<String> candidates = new LinkedHashSet<>();
                if (timeSlot == null) {
                        candidates.add("");
                        return new ArrayList<>(candidates);
                }

                String trimmed = String.valueOf(timeSlot).trim();
                if (!trimmed.isEmpty()) {
                        candidates.add(trimmed);
                        candidates.add(trimmed.replace(" ", ""));
                }

                String normalized = normalizeTimeSlotForQuery(trimmed);
                if (!normalized.isEmpty()) {
                        candidates.add(normalized);
                }

                return new ArrayList<>(candidates);
        }

        private String formatSlotMinute(int minute) {
                if (minute == 1440) {
                        return "24:00";
                }
                int h = minute / 60;
                int m = minute % 60;
                return String.format("%02d:%02d", h, m);
        }

        private LocalTime[] parseTimeSlotRange(String timeSlot) {
                if (timeSlot == null) {
                        throw new BusinessException(ErrorCode.INVALID_TIME_SLOT);
                }

                String[] split = timeSlot.split("~");
                if (split.length != 2) {
                        throw new BusinessException(ErrorCode.INVALID_TIME_SLOT);
                }

                String startTimeText = split[0].trim();
                String endTimeText = split[1].trim();

                LocalTime startTime = parseTime(startTimeText);
                LocalTime endTime = parseTime(endTimeText);

                int startMinute = toMinuteForValidation(startTimeText, false);
                int endMinute = toMinuteForValidation(endTimeText, true);

                if (startMinute == endMinute
                        || endMinute <= startMinute
                        || startMinute < 0
                        || endMinute < 0) {
                        throw new BusinessException(ErrorCode.INVALID_TIME_SLOT);
                }

                return new LocalTime[] { startTime, endTime };
        }

        private int toMinuteForValidation(String value, boolean isEndTime) {
                String normalized = value == null ? "" : value.trim();

                if ("24:00".equals(normalized)) {
                        return 1440;
                }

                if ("00:00".equals(normalized)) {
                        return isEndTime ? 1440 : 0;
                }

                return toMinuteForValidation(parseTime(normalized));
        }

        private int toMinuteForValidation(LocalTime value) {
                if (value == null) {
                        return -1;
                }

                return value.getHour() * 60 + value.getMinute();
        }

        private LocalTime parseTime(String value) {
                String normalized = value == null ? "" : value.trim();

                try {
                        return LocalTime.parse(normalized, DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception e) {
                        if ("24:00".equals(normalized)) {
                                return LocalTime.MIDNIGHT;
                        }

                        throw new BusinessException(ErrorCode.INVALID_TIME_SLOT);
                }
        }
}
