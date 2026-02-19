package com.pickleball.controller;

import com.pickleball.dto.CirclePlaceDto;
import com.pickleball.service.CirclePlaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/circle-manage/places")
public class CirclePlaceController {

    private final CirclePlaceService circlePlaceService;

    public CirclePlaceController(CirclePlaceService circlePlaceService) {
        this.circlePlaceService = circlePlaceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CIRCLE')")
    public ResponseEntity<List<CirclePlaceDto.Response>> getMyPlaces(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(circlePlaceService.getMyPlaces(userDetails.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasRole('CIRCLE')")
    public ResponseEntity<CirclePlaceDto.Response> createPlace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CirclePlaceDto.Request request) {
        return ResponseEntity.ok(circlePlaceService.createPlace(userDetails.getUsername(), request));
    }
}
