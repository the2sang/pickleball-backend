package com.pickleball.controller;

import com.pickleball.dto.PartnerReservationDto;
import com.pickleball.service.PartnerReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/partner-manage/reservation")
@RequiredArgsConstructor
@Tag(name = "Partner Reservation Management", description = "사업장 코트 예약 설정 API")
public class PartnerReservationController {

    private final PartnerReservationService partnerReservationService;

    @Operation(summary = "예약 설정 조회", description = "특정 코트와 날짜의 예약 설정(시간대별 운영 타입)을 조회합니다.")
    @GetMapping("/settings")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<PartnerReservationDto.SettingResponse> getSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(partnerReservationService.getSettings(userDetails.getUsername(), courtId, date));
    }

    @Operation(summary = "예약 설정 저장", description = "특정 코트와 날짜의 시간대별 운영 타입을 저장합니다.")
    @PostMapping("/settings")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<Void> saveSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PartnerReservationDto.SettingRequest request) {
        partnerReservationService.saveSettings(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
