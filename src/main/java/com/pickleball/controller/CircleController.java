package com.pickleball.controller;

import com.pickleball.dto.CircleDto;
import com.pickleball.service.CircleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/circles")
@RequiredArgsConstructor
public class CircleController {

    private final CircleService circleService;

    @GetMapping
    public ResponseEntity<CircleDto.PageResponse<CircleDto.Response>> getCircles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(circleService.getCircles(keyword, page, size));
    }
}
