package com.pickleball.service;

import com.pickleball.dto.PartnerDto;
import com.pickleball.entity.Account;
import com.pickleball.entity.Court;
import com.pickleball.entity.FavoritePartner;
import com.pickleball.exception.BusinessException;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.CourtRepository;
import com.pickleball.repository.FavoritePartnerRepository;
import com.pickleball.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private static final int MAX_PARTNER_BOX_COUNT = 6;
    private static final int MAX_FAVORITE_COUNT = 6;

    private final PartnerRepository partnerRepository;
    private final CourtRepository courtRepository;
    private final FavoritePartnerRepository favoritePartnerRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public PartnerDto.PageResponse<PartnerDto.Response> getPartners(
            String keyword, int page, int size, String username) {

        String normalizedKeyword = keyword == null ? null : keyword.trim();

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int limitedSize = Math.min(safeSize, MAX_PARTNER_BOX_COUNT);
        int offset = (safePage - 1) * limitedSize;

        String normalizedUsername = normalizeMemberUsername(username);
        boolean useMyUsageRanking = normalizedUsername != null
                && favoritePartnerRepository.countByUsername(normalizedUsername) > 0;

        List<PartnerDto.Response> data = partnerRepository
                .findMainPartners(normalizedKeyword, normalizedUsername, useMyUsageRanking, limitedSize, offset)
                .stream()
                .map(row -> PartnerDto.Response.builder()
                        .id(row.getId())
                        .businessPartner(row.getBusinessPartner())
                        .owner(row.getOwner())
                        .phoneNumber(row.getPhoneNumber())
                        .partnerAddress(row.getPartnerAddress())
                        .courtCount(row.getCourtCount())
                        .reservationCount(row.getReservationCount())
                        .myReservationCount(row.getMyReservationCount())
                        .favorite(row.getFavorite())
                        .build())
                .toList();

        long total = partnerRepository.countMainPartners(normalizedKeyword);

        return PartnerDto.PageResponse.<PartnerDto.Response>builder()
                .data(data)
                .total(total)
                .page(safePage)
                .size(limitedSize)
                .build();
    }

    @Transactional
    public void addFavoritePartner(String username, Long partnerId) {
        String memberUsername = requireMemberUsername(username);

        if (!partnerRepository.existsById(partnerId)) {
            throw new BusinessException(BusinessException.ErrorCode.PARTNER_NOT_FOUND);
        }
        if (favoritePartnerRepository.existsByUsernameAndPartnerId(memberUsername, partnerId)) {
            return;
        }
        long favoriteCount = favoritePartnerRepository.countByUsername(memberUsername);
        if (favoriteCount >= MAX_FAVORITE_COUNT) {
            throw new BusinessException(BusinessException.ErrorCode.FAVORITE_LIMIT_EXCEEDED);
        }

        favoritePartnerRepository.save(FavoritePartner.builder()
                .username(memberUsername)
                .partnerId(partnerId)
                .build());
    }

    @Transactional
    public void removeFavoritePartner(String username, Long partnerId) {
        String memberUsername = requireMemberUsername(username);
        favoritePartnerRepository.deleteByUsernameAndPartnerId(memberUsername, partnerId);
    }

    @Transactional(readOnly = true)
    public List<Court> getCourtsByPartner(Long partnerId) {
        return courtRepository.findByPartnerIdOrderByCourtName(partnerId);
    }

    private String normalizeMemberUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        if (account == null || !"MEMBER".equalsIgnoreCase(account.getAccountType())) {
            return null;
        }
        return account.getUsername();
    }

    private String requireMemberUsername(String username) {
        String memberUsername = normalizeMemberUsername(username);
        if (memberUsername == null) {
            throw new BusinessException(BusinessException.ErrorCode.FAVORITE_MEMBER_ONLY);
        }
        return memberUsername;
    }
}
