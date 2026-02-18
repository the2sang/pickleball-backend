package com.pickleball.controller;

import com.pickleball.dto.AdminDto;
import com.pickleball.dto.PartnerDto;
import com.pickleball.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 전체 사업장 목록 조회
     * GET /api/v1/admin/partners?keyword=&page=1&size=20
     */
    @GetMapping("/partners")
    public ResponseEntity<PartnerDto.PageResponse<AdminDto.PartnerDetailResponse>> getPartners(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllPartners(keyword, page, size));
    }

    /**
     * 사업장 상세 조회
     * GET /api/v1/admin/partners/{id}
     */
    @GetMapping("/partners/{id}")
    public ResponseEntity<AdminDto.PartnerDetailResponse> getPartnerDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPartnerDetail(id));
    }

    /**
     * 사업장 정보 수정
     * PUT /api/v1/admin/partners/{id}
     */
    @PutMapping("/partners/{id}")
    public ResponseEntity<AdminDto.PartnerDetailResponse> updatePartner(
            @PathVariable Long id,
            @Valid @RequestBody AdminDto.PartnerUpdateRequest request) {
        return ResponseEntity.ok(adminService.updatePartner(id, request));
    }

    /**
     * 사업장 승인 (partnerLevel 0 → 1)
     * PUT /api/v1/admin/partners/{id}/approve
     */
    @PutMapping("/partners/{id}/approve")
    public ResponseEntity<AdminDto.PartnerDetailResponse> approvePartner(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approvePartner(id));
    }

    @GetMapping("/circles")
    public ResponseEntity<PartnerDto.PageResponse<AdminDto.PartnerDetailResponse>> getCircles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllCircles(keyword, page, size));
    }

    @GetMapping("/circles/{id}")
    public ResponseEntity<AdminDto.PartnerDetailResponse> getCircleDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getCircleDetail(id));
    }

    @PutMapping("/circles/{id}")
    public ResponseEntity<AdminDto.PartnerDetailResponse> updateCircle(
            @PathVariable Long id,
            @Valid @RequestBody AdminDto.PartnerUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateCircle(id, request));
    }

    @PutMapping("/circles/{id}/approve")
    public ResponseEntity<AdminDto.PartnerDetailResponse> approveCircle(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveCircle(id));
    }
}
