package com.pickleball.service;

import com.pickleball.dto.MemberDto;
import com.pickleball.entity.Account;
import com.pickleball.entity.Member;
import com.pickleball.exception.BusinessException;
import com.pickleball.exception.BusinessException.ErrorCode;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    /**
     * 본인 프로필 조회
     */
    @Transactional(readOnly = true)
    public MemberDto.ProfileResponse getProfile(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member member = memberRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return toProfileResponse(account, member);
    }

    /**
     * 본인 프로필 수정
     */
    @Transactional
    public MemberDto.ProfileResponse updateProfile(String username, MemberDto.ProfileUpdateRequest request) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member member = memberRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // Account 이름도 동기화
        account.setName(request.getName());
        accountRepository.save(account);

        // Member 프로필 수정
        member.setName(request.getName());
        member.setPhoneNumber(request.getPhoneNumber());
        member.setNicName(request.getNicName());
        member.setEmail(request.getEmail());
        member.setSex(request.getSex());
        member.setAgeRange(request.getAgeRange());
        member.setLocation(request.getLocation());
        member.setCircleName(request.getCircleName());
        member.setGameLevel(request.getGameLevel());
        member.setDuprPoint(request.getDuprPoint());
        memberRepository.save(member);

        return toProfileResponse(account, member);
    }

    private MemberDto.ProfileResponse toProfileResponse(Account account, Member member) {
        return MemberDto.ProfileResponse.builder()
                .id(member.getId())
                .username(account.getUsername())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .nicName(member.getNicName())
                .email(member.getEmail())
                .sex(member.getSex())
                .ageRange(member.getAgeRange())
                .location(member.getLocation())
                .circleName(member.getCircleName())
                .gameLevel(member.getGameLevel())
                .duprPoint(member.getDuprPoint())
                .memberLevel(member.getMemberLevel())
                .registDate(member.getRegistDate() != null ? member.getRegistDate().toString() : null)
                .build();
    }
}
