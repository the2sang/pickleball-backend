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
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

        private final ReservationRepository reservationRepository;
        private final CourtRepository courtRepository;
        private final MemberSuspensionRepository suspensionRepository;
        private final RejectVoteRepository rejectVoteRepository;
        private final MemberRepository memberRepository;
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

                List<ReservationDto.SlotInfo> slots = schedules.stream()
                                .map(schedule -> {
                                        String timeSlot = formatTimeSlot(schedule.getStartTime(),
                                                        schedule.getEndTime());
                                        List<Reservation> reservations = reservationRepository
                                                        .findActivePlayers(courtId, gameDate, timeSlot);

                                        int count = reservations.size();
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
                                        List<ReservationDto.PlayerInfo> players = new java.util.ArrayList<>();
                                        for (int i = 0; i < reservations.size(); i++) {
                                                Reservation r = reservations.get(i);
                                                ReservationDto.PlayerInfo playerInfo = buildPlayerInfo(r);
                                                playerInfo.setOrderNumber(i + 1);
                                                playerInfo.setWaiting(i >= capacity); // 정원 초과는 대기
                                                players.add(playerInfo);
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

                List<Reservation> reservations = reservationRepository.findActivePlayers(courtId, gameDate, timeSlot);
                List<ReservationDto.PlayerInfo> players = new java.util.ArrayList<>();

                for (int i = 0; i < reservations.size(); i++) {
                        Reservation r = reservations.get(i);
                        ReservationDto.PlayerInfo playerInfo = buildPlayerInfo(r);
                        playerInfo.setOrderNumber(i + 1);
                        playerInfo.setWaiting(i >= capacity);
                        players.add(playerInfo);
                }

                return players;
        }

        /**
         * 예약 정보에서 PlayerInfo 빌드 (Account + Member 조회)
         */
        private ReservationDto.PlayerInfo buildPlayerInfo(Reservation reservation) {
                Account account = reservation.getAccount();
                Member member = memberRepository.findByAccountId(account.getId()).orElse(null);

                return ReservationDto.PlayerInfo.builder()
                                .reservationId(reservation.getId())
                                .username(account.getUsername())
                                .name(account.getName())
                                .nicName(member != null ? member.getNicName() : null)
                                .gameLevel(member != null ? member.getGameLevel() : null)
                                .duprPoint(member != null ? member.getDuprPoint() : null)
                                .sex(member != null ? member.getSex() : null)
                                .reservedAt(reservation.getCreateDate())
                                .build();
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

                // 3. 중복 예약 확인
                reservationRepository.findByUsernameAndCourtIdAndGameDateAndTimeSlotAndCancelYnNot(
                                username, request.getCourtId(), request.getGameDate(),
                                request.getTimeSlot(), "Y")
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
                                LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm")));
        }
}
