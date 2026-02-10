package com.pickleball.service;

import com.pickleball.dto.PartnerDto;
import com.pickleball.entity.Court;
import com.pickleball.repository.CourtRepository;
import com.pickleball.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final CourtRepository courtRepository;

    @Transactional(readOnly = true)
    public PartnerDto.PageResponse<PartnerDto.Response> getPartners(
            String keyword, int page, int size) {

        var pageable = PageRequest.of(page - 1, size);
        var partnerPage = (keyword != null && !keyword.isBlank())
                ? partnerRepository.searchPartners(keyword, pageable)
                : partnerRepository.findActivePartners(pageable);

        List<PartnerDto.Response> data = partnerPage.getContent().stream()
                .map(p -> {
                    long courtCount = courtRepository
                            .findByPartnerIdOrderByCourtName(p.getId()).size();
                    return PartnerDto.Response.builder()
                            .id(p.getId())
                            .businessPartner(p.getBusinessPartner())
                            .owner(p.getOwner())
                            .phoneNumber(p.getPhoneNumber())
                            .partnerAddress(p.getPartnerAddress())
                            .courtCount(courtCount)
                            .build();
                })
                .toList();

        return PartnerDto.PageResponse.<PartnerDto.Response>builder()
                .data(data)
                .total(partnerPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Court> getCourtsByPartner(Long partnerId) {
        return courtRepository.findByPartnerIdOrderByCourtName(partnerId);
    }
}
