package com.pickleball.service;

import com.pickleball.dto.AdminDto;
import com.pickleball.dto.PartnerDto;
import com.pickleball.entity.Partner;
import com.pickleball.exception.BusinessException;
import com.pickleball.exception.BusinessException.ErrorCode;
import com.pickleball.repository.CourtRepository;
import com.pickleball.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PartnerRepository partnerRepository;
    private final CourtRepository courtRepository;

    /**
     * 전체 사업장 목록 조회 (관리자용 - partnerLevel 필터 없음)
     */
    @Transactional(readOnly = true)
    public PartnerDto.PageResponse<AdminDto.PartnerDetailResponse> getAllPartners(
            String keyword, int page, int size) {

        var pageable = PageRequest.of(page - 1, size);
        var partnerPage = (keyword != null && !keyword.isBlank())
                ? partnerRepository.searchAllPartners(keyword, pageable)
                : partnerRepository.findAllPartners(pageable);

        List<AdminDto.PartnerDetailResponse> data = partnerPage.getContent().stream()
                .map(this::toDetailResponse)
                .toList();

        return PartnerDto.PageResponse.<AdminDto.PartnerDetailResponse>builder()
                .data(data)
                .total(partnerPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    /**
     * 사업장 상세 조회
     */
    @Transactional(readOnly = true)
    public AdminDto.PartnerDetailResponse getPartnerDetail(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return toDetailResponse(partner);
    }

    /**
     * 사업장 정보 수정 (아이디/패스워드 제외)
     */
    @Transactional
    public AdminDto.PartnerDetailResponse updatePartner(Long id, AdminDto.PartnerUpdateRequest request) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        partner.setBusinessPartner(request.getBusinessPartner());
        partner.setOwner(request.getOwner());
        partner.setPhoneNumber(request.getPhoneNumber());
        partner.setPartnerAddress(request.getPartnerAddress());
        partner.setPartnerEmail(request.getPartnerEmail());
        partner.setPartnerAccount(request.getPartnerAccount());
        partner.setPartnerBank(request.getPartnerBank());
        partner.setHowToPay(request.getHowToPay());

        partnerRepository.save(partner);
        return toDetailResponse(partner);
    }

    /**
     * 사업장 승인 (partnerLevel 0 → 1)
     */
    @Transactional
    public AdminDto.PartnerDetailResponse approvePartner(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        partner.setPartnerLevel("1");
        partnerRepository.save(partner);
        return toDetailResponse(partner);
    }

    private AdminDto.PartnerDetailResponse toDetailResponse(Partner p) {
        long courtCount = courtRepository
                .findByPartnerIdOrderByCourtName(p.getId()).size();

        return AdminDto.PartnerDetailResponse.builder()
                .id(p.getId())
                .accountId(p.getAccountId())
                .businessPartner(p.getBusinessPartner())
                .owner(p.getOwner())
                .phoneNumber(p.getPhoneNumber())
                .partnerAddress(p.getPartnerAddress())
                .partnerLevel(p.getPartnerLevel())
                .partnerEmail(p.getPartnerEmail())
                .registDate(p.getRegistDate() != null ? p.getRegistDate().toString() : null)
                .partnerAccount(p.getPartnerAccount())
                .partnerBank(p.getPartnerBank())
                .howToPay(p.getHowToPay())
                .courtCount(courtCount)
                .build();
    }
}
