package com.pickleball.service;

import com.pickleball.dto.PartnerCourtDto;
import com.pickleball.entity.Account;
import com.pickleball.entity.Court;
import com.pickleball.entity.Partner;
import com.pickleball.exception.BusinessException;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.CourtRepository;
import com.pickleball.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerCourtService {

    private final CourtRepository courtRepository;
    private final PartnerRepository partnerRepository;
    private final AccountRepository accountRepository;

    private Partner getPartnerByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.MEMBER_NOT_FOUND));

        return partnerRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.MEMBER_NOT_FOUND));
    }

    public List<PartnerCourtDto.Response> getMyCourts(String username) {
        Partner partner = getPartnerByUsername(username);
        return courtRepository.findAllByPartnerIdOrderByIdDesc(partner.getId()).stream()
                .map(PartnerCourtDto.Response::from)
                .collect(Collectors.toList());
    }

    public PartnerCourtDto.Response getCourt(String username, Long courtId) {
        Partner partner = getPartnerByUsername(username);
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        return PartnerCourtDto.Response.from(court);
    }

    @Transactional
    public PartnerCourtDto.Response createCourt(String username, PartnerCourtDto.Request request) {
        Partner partner = getPartnerByUsername(username);

        Court court = Court.builder()
                .partnerId(partner.getId())
                .courtName(request.getCourtName())
                .personnelNumber(request.getPersonnelNumber())
                .courtLevel(request.getCourtLevel())
                .gameTime(request.getGameTime())
                .reservClose(request.getReservClose() == null ? "N" : request.getReservClose())
                .courtGugun(request.getCourtGugun() == null ? "01" : request.getCourtGugun())
                .build();

        courtRepository.save(court);
        return PartnerCourtDto.Response.from(court);
    }

    @Transactional
    public PartnerCourtDto.Response updateCourt(String username, Long courtId, PartnerCourtDto.Request request) {
        Partner partner = getPartnerByUsername(username);
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        court.setCourtName(request.getCourtName());
        court.setPersonnelNumber(request.getPersonnelNumber());
        court.setCourtLevel(request.getCourtLevel());
        court.setGameTime(request.getGameTime());
        court.setReservClose(request.getReservClose());
        court.setCourtGugun(request.getCourtGugun());

        return PartnerCourtDto.Response.from(court);
    }

    @Transactional
    public void deleteCourt(String username, Long courtId) {
        Partner partner = getPartnerByUsername(username);
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.COURT_NOT_FOUND));

        if (!court.getPartnerId().equals(partner.getId())) {
            throw new BusinessException(BusinessException.ErrorCode.NOT_OWNER);
        }

        courtRepository.delete(court);
    }
}
