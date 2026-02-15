package com.pickleball.controller;

import com.pickleball.dto.RentalRequestDto;
import com.pickleball.service.RentalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rentals/requests")
@RequiredArgsConstructor
public class RentalRequestController {

    private final RentalRequestService rentalRequestService;

    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<RentalRequestDto.Response> create(
            @Valid @RequestBody RentalRequestDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(201).body(
                rentalRequestService.createRequest(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<List<RentalRequestDto.Response>> my(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(rentalRequestService.getMyRequests(userDetails.getUsername()));
    }
}
