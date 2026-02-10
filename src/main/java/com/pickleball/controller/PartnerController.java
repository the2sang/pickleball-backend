package com.pickleball.controller;

import com.pickleball.dto.PartnerDto;
import com.pickleball.entity.Court;
import com.pickleball.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    /**
     * 사업장 목록 조회
     * GET /api/v1/partners?keyword=서울&page=1&size=20
     */
    @GetMapping
    public ResponseEntity<PartnerDto.PageResponse<PartnerDto.Response>> getPartners(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(partnerService.getPartners(keyword, page, size));
    }

    /**
     * 사업장별 코트 목록 조회
     * GET /api/v1/partners/{id}/courts
     */
    @GetMapping("/{id}/courts")
    public ResponseEntity<List<Court>> getCourts(@PathVariable Long id) {
        return ResponseEntity.ok(partnerService.getCourtsByPartner(id));
    }
}
