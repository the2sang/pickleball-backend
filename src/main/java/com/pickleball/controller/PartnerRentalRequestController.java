package com.pickleball.controller;

import com.pickleball.dto.RentalRequestDto;
import com.pickleball.service.RentalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/partner-manage/rentals/requests")
@RequiredArgsConstructor
public class PartnerRentalRequestController {

    private final RentalRequestService rentalRequestService;

    @GetMapping
    public ResponseEntity<List<RentalRequestDto.PartnerListItem>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                rentalRequestService.getPartnerRequests(userDetails.getUsername(), courtId, date, status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RentalRequestDto.Response> approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(rentalRequestService.approve(userDetails.getUsername(), id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RentalRequestDto.Response> reject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) RentalRequestDto.RejectRequest body) {
        return ResponseEntity.ok(rentalRequestService.reject(userDetails.getUsername(), id));
    }
}
