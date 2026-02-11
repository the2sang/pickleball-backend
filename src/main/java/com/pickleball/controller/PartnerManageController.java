package com.pickleball.controller;

import com.pickleball.dto.PartnerProfileDto;
import com.pickleball.entity.Account;
import com.pickleball.entity.Partner;
import com.pickleball.exception.BusinessException;
import com.pickleball.exception.BusinessException.ErrorCode;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.PartnerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/partner-manage")
@RequiredArgsConstructor
public class PartnerManageController {

    private final AccountRepository accountRepository;
    private final PartnerRepository partnerRepository;

    /**
     * 본인 사업장 프로필 조회
     * GET /api/v1/partner-manage/me
     */
    @GetMapping("/me")
    public ResponseEntity<PartnerProfileDto.Response> getMyProfile(Authentication authentication) {
        Partner partner = findPartnerByUsername(authentication.getName());
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return ResponseEntity.ok(toResponse(account, partner));
    }

    /**
     * 본인 사업장 프로필 수정
     * PUT /api/v1/partner-manage/me
     */
    @PutMapping("/me")
    public ResponseEntity<PartnerProfileDto.Response> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody PartnerProfileDto.UpdateRequest request) {

        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Partner partner = findPartnerByUsername(authentication.getName());

        // Account 이름 동기화
        account.setName(request.getOwner());
        accountRepository.save(account);

        // Partner 정보 수정
        partner.setBusinessPartner(request.getBusinessPartner());
        partner.setOwner(request.getOwner());
        partner.setPhoneNumber(request.getPhoneNumber());
        partner.setPartnerAddress(request.getPartnerAddress());
        partner.setPartnerEmail(request.getPartnerEmail());
        partner.setPartnerAccount(request.getPartnerAccount());
        partner.setPartnerBank(request.getPartnerBank());
        partner.setHowToPay(request.getHowToPay());
        partnerRepository.save(partner);

        return ResponseEntity.ok(toResponse(account, partner));
    }

    private Partner findPartnerByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return partnerRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private PartnerProfileDto.Response toResponse(Account account, Partner partner) {
        return PartnerProfileDto.Response.builder()
                .id(partner.getId())
                .username(account.getUsername())
                .businessPartner(partner.getBusinessPartner())
                .owner(partner.getOwner())
                .phoneNumber(partner.getPhoneNumber())
                .partnerAddress(partner.getPartnerAddress())
                .partnerLevel(partner.getPartnerLevel())
                .partnerEmail(partner.getPartnerEmail())
                .registDate(partner.getRegistDate() != null ? partner.getRegistDate().toString() : null)
                .partnerAccount(partner.getPartnerAccount())
                .partnerBank(partner.getPartnerBank())
                .howToPay(partner.getHowToPay())
                .build();
    }
}
