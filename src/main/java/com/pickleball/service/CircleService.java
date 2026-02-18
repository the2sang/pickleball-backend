package com.pickleball.service;

import com.pickleball.dto.CircleDto;
import com.pickleball.repository.CircleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CircleService {

    private final CircleRepository circleRepository;

    @Transactional(readOnly = true)
    public CircleDto.PageResponse<CircleDto.Response> getCircles(
            String keyword, int page, int size) {

        var pageable = PageRequest.of(page - 1, size);
        var circlePage = (keyword != null && !keyword.isBlank())
                ? circleRepository.searchCircles(keyword, pageable)
                : circleRepository.findActiveCircles(pageable);

        List<CircleDto.Response> data = circlePage.getContent().stream()
                .map(c -> CircleDto.Response.builder()
                        .id(c.getId())
                        .businessPartner(c.getBusinessPartner())
                        .owner(c.getOwner())
                        .phoneNumber(c.getPhoneNumber())
                        .partnerAddress(c.getPartnerAddress())
                        .courtCount(0)
                        .build())
                .toList();

        return CircleDto.PageResponse.<CircleDto.Response>builder()
                .data(data)
                .total(circlePage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional(readOnly = true)
    public CircleDto.PageResponse<CircleDto.Response> getCircleOptions(
            String keyword, int page, int size) {

        var pageable = PageRequest.of(page - 1, size);
        var circlePage = (keyword != null && !keyword.isBlank())
                ? circleRepository.searchAllCircles(keyword, pageable)
                : circleRepository.findAllCircles(pageable);

        List<CircleDto.Response> data = circlePage.getContent().stream()
                .map(c -> CircleDto.Response.builder()
                        .id(c.getId())
                        .businessPartner(c.getBusinessPartner())
                        .owner(c.getOwner())
                        .phoneNumber(c.getPhoneNumber())
                        .partnerAddress(c.getPartnerAddress())
                        .courtCount(0)
                        .build())
                .toList();

        return CircleDto.PageResponse.<CircleDto.Response>builder()
                .data(data)
                .total(circlePage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
}
