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

    private static final List<String> DEFAULT_TIME_SLOTS = Arrays.asList(
            "06:00~08:00", "08:00~10:00", "10:00~12:00", "12:00~14:00",
            "14:00~16:00", "16:00~18:00", "18:00~20:00", "20:00~22:00"
    );

    /**
     * 코트별 시간대 예약 현황 조회
     */
    @Transactional(readOnly = true)
    public ReservationDto.CourtSlotResponse getCourtSlots(Long courtId, LocalDate gameDate) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURT_NOT_FOUND));

        List<ReservationDto.SlotInfo> slots = DEFAULT_TIME_SLOTS.stream()
                .map(timeSlot -> {
                    List<Reservation> reservations = reservationRepository
                            .findActivePlayers(courtId, gameDate, timeSlot);

                    int count = reservations.size();
                    int capacity = court.getPersonnelNumber() != null ? court.getPersonnelNumber() : 6;
                    String status = count >= capacity ? "FULL" : "AVAILABLE";

                    if ("Y".equals(court.getReservClose())) {
                        status = "CLOSED";
                    }

                    List<ReservationDto.PlayerInfo> players = reservations.stream()
                            .map(r -> ReservationDto.PlayerInfo.builder()
                                    .name(r.getMember().getName())
                                    .nicName(r.getMember().getNicName())
                                    .gameLevel(r.getMember().getGameLevel())
                                    .duprPoint(r.getMember().getDuprPoint())
                                    .sex(r.getMember().getSex())
                                    .reservedAt(r.getCreateDate())
                                    .build())
                            .toList();

                    return ReservationDto.SlotInfo.builder()
                            .timeSlot(timeSlot)
                            .reservedCount(count)
                            .capacity(capacity)
                            .status(status)
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

    /**
     * 특정 시간대 예약자 상세 조회
     */
    @Transactional(readOnly = true)
    public List<ReservationDto.PlayerInfo> getSlotPlayers(
            Long courtId, LocalDate gameDate, String timeSlot) {
        return reservationRepository.findActivePlayers(courtId, gameDate, timeSlot)
                .stream()
                .map(r -> ReservationDto.PlayerInfo.builder()
                        .name(r.getMember().getName())
                        .nicName(r.getMember().getNicName())
                        .gameLevel(r.getMember().getGameLevel())
                        .duprPoint(r.getMember().getDuprPoint())
                        .sex(r.getMember().getSex())
                        .reservedAt(r.getCreateDate())
                        .build())
                .toList();
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

        // 게임 개시 4시간 전 체크
        LocalDateTime gameStart = parseGameStart(
                reservation.getGameDate(), reservation.getTimeSlot());
        LocalDateTime deadline = gameStart.minusHours(4);

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
