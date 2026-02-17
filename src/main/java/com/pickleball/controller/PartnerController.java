package com.pickleball.controller;

import com.pickleball.dto.CourtDto;
import com.pickleball.dto.PartnerDto;
import com.pickleball.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(partnerService.getPartners(keyword, page, size, username));
    }

    @PostMapping("/{id}/favorites")
    public ResponseEntity<Void> addFavoritePartner(@PathVariable Long id, Authentication authentication) {
        partnerService.addFavoritePartner(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/favorites")
    public ResponseEntity<Void> removeFavoritePartner(@PathVariable Long id, Authentication authentication) {
        partnerService.removeFavoritePartner(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 사업장별 코트 목록 조회
     * GET /api/v1/partners/{id}/courts
     */
    @GetMapping("/{id}/courts")
    public ResponseEntity<List<CourtDto.Response>> getCourts(@PathVariable Long id) {
        return ResponseEntity.ok(partnerService.getCourtsByPartner(id)
                .stream()
                .map(CourtDto.Response::from)
                .toList());
    }
}
