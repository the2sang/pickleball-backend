package com.pickleball.controller;

import com.pickleball.dto.ReservationDto;
import com.pickleball.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 코트별 시간대 예약 현황 조회
     * GET /api/v1/courts/{courtId}/slots?date=2026-02-10
     */
    @GetMapping("/courts/{courtId}/slots")
    public ResponseEntity<ReservationDto.CourtSlotResponse> getCourtSlots(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.getCourtSlots(courtId, date));
    }

    /**
     * 특정 시간대 예약자 상세 조회
     * GET /api/v1/courts/{courtId}/slots/{timeSlot}/players?date=2026-02-10
     */
    @GetMapping("/courts/{courtId}/slots/{timeSlot}/players")
    public ResponseEntity<List<ReservationDto.PlayerInfo>> getSlotPlayers(
            @PathVariable Long courtId,
            @PathVariable String timeSlot,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                reservationService.getSlotPlayers(courtId, date, timeSlot));
    }

    /**
     * 예약 신청
     * POST /api/v1/reservations
     */
    @PostMapping("/reservations")
    public ResponseEntity<ReservationDto.Response> createReservation(
            @Valid @RequestBody ReservationDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(201).body(
                reservationService.createReservation(request, userDetails.getUsername()));
    }

    /**
     * 예약 취소
     * DELETE /api/v1/reservations/{id}
     */
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        reservationService.cancelReservation(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 예약 목록 조회
     * GET /api/v1/members/me/reservations
     */
    @GetMapping("/members/me/reservations")
    public ResponseEntity<List<ReservationDto.Response>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                reservationService.getMyReservations(userDetails.getUsername()));
    }
}
