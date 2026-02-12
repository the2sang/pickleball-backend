package com.pickleball.controller;

import com.pickleball.dto.PartnerCourtDto;
import com.pickleball.service.PartnerCourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partner-manage/courts")
@RequiredArgsConstructor
@Tag(name = "Partner Court Management", description = "사업장 코트 관리 API")
public class PartnerCourtController {

    private final PartnerCourtService partnerCourtService;

    @Operation(summary = "내 코트 목록 조회", description = "본인 사업장의 코트 목록을 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<List<PartnerCourtDto.Response>> getMyCourts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(partnerCourtService.getMyCourts(userDetails.getUsername()));
    }

    @Operation(summary = "코트 등록", description = "새로운 코트를 등록합니다.")
    @PostMapping
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<PartnerCourtDto.Response> createCourt(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PartnerCourtDto.Request request) {
        return ResponseEntity.ok(partnerCourtService.createCourt(userDetails.getUsername(), request));
    }

    @Operation(summary = "코트 상세 조회")
    @GetMapping("/{courtId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<PartnerCourtDto.Response> getCourt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courtId) {
        return ResponseEntity.ok(partnerCourtService.getCourt(userDetails.getUsername(), courtId));
    }

    @Operation(summary = "코트 정보 수정")
    @PutMapping("/{courtId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<PartnerCourtDto.Response> updateCourt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courtId,
            @RequestBody PartnerCourtDto.Request request) {
        return ResponseEntity.ok(partnerCourtService.updateCourt(userDetails.getUsername(), courtId, request));
    }

    @Operation(summary = "코트 삭제")
    @DeleteMapping("/{courtId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<Void> deleteCourt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courtId) {
        partnerCourtService.deleteCourt(userDetails.getUsername(), courtId);
        return ResponseEntity.ok().build();
    }
}
