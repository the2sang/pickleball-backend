package com.pickleball.service;

import com.pickleball.dto.AuthDto;
import com.pickleball.entity.Account;
import com.pickleball.entity.Member;
import com.pickleball.entity.MemberRole;
import com.pickleball.entity.Partner;
import com.pickleball.exception.BusinessException;
import com.pickleball.exception.BusinessException.ErrorCode;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.MemberRepository;
import com.pickleball.repository.MemberRoleRepository;
import com.pickleball.repository.PartnerRepository;
import com.pickleball.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final AccountRepository accountRepository;
        private final MemberRepository memberRepository;
        private final MemberRoleRepository memberRoleRepository;
        private final PartnerRepository partnerRepository;
        private final JwtTokenProvider jwtTokenProvider;
        private final PasswordEncoder passwordEncoder;

        public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername(), request.getPassword()));

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Account account = accountRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

                String accessToken = jwtTokenProvider.generateToken(userDetails);
                String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

                List<String> roles = userDetails.getAuthorities().stream()
                                .map(a -> a.getAuthority())
                                .toList();

                return AuthDto.TokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(3600)
                                .username(account.getUsername())
                                .name(account.getName())
                                .accountType(account.getAccountType())
                                .roles(roles)
                                .build();
        }

        @Transactional
        public AuthDto.TokenResponse signup(AuthDto.SignupRequest request) {
                if (accountRepository.existsByUsername(request.getUsername())) {
                        throw new BusinessException(ErrorCode.USERNAME_EXISTS);
                }

                if (!Boolean.TRUE.equals(request.getAgreeService()) || !Boolean.TRUE.equals(request.getAgreePrivacy())) {
                        throw new BusinessException(ErrorCode.TERMS_REQUIRED);
                }

                // 1. Account 생성
                Account account = Account.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .accountType("MEMBER")
                                .name(request.getName())
                                .build();
                accountRepository.save(account);

                // 2. Member 프로필 생성
                Member member = Member.builder()
                                .accountId(account.getId())
                                .name(request.getName())
                                .phoneNumber(request.getPhoneNumber())
                                .nicName(request.getNicName())
                                .email(request.getEmail())
                                .sex(request.getSex())
                                .ageRange(request.getAgeRange())
                                .location(request.getLocation())
                                .circleName(request.getCircleName())
                                .gameLevel(request.getGameLevel() != null ? request.getGameLevel() : "입문")
                                .duprPoint(request.getDuprPoint())
                                .memberLevel("정회원")
                                .registDate(LocalDate.now())
                                .agreeServiceYn(Boolean.TRUE.equals(request.getAgreeService()) ? "Y" : "N")
                                .agreePrivacyYn(Boolean.TRUE.equals(request.getAgreePrivacy()) ? "Y" : "N")
                                .agreeMarketingYn(Boolean.TRUE.equals(request.getAgreeMarketing()) ? "Y" : "N")
                                .agreeAllYn(Boolean.TRUE.equals(request.getAgreeAll()) ? "Y" : "N")
                                .build();
                memberRepository.save(member);

                // 3. 권한 부여
                MemberRole role = MemberRole.builder()
                                .username(request.getUsername())
                                .roles("ROLE_USER")
                                .build();
                memberRoleRepository.save(role);

                // 4. 자동 로그인
                return login(new AuthDto.LoginRequest(
                                request.getUsername(), request.getPassword()));
        }

        @Transactional
        public AuthDto.TokenResponse signupPartner(AuthDto.PartnerSignupRequest request) {
                if (accountRepository.existsByUsername(request.getUsername())) {
                        throw new BusinessException(ErrorCode.USERNAME_EXISTS);
                }

                if (!Boolean.TRUE.equals(request.getAgreeService()) || !Boolean.TRUE.equals(request.getAgreePrivacy())) {
                        throw new BusinessException(ErrorCode.TERMS_REQUIRED);
                }

                // 1. Account 생성
                Account account = Account.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .accountType("PARTNER")
                                .name(request.getName())
                                .build();
                accountRepository.save(account);

                // 2. Partner 프로필 생성
                Partner partner = Partner.builder()
                                .accountId(account.getId())
                                .businessPartner(request.getBusinessPartner())
                                .owner(request.getOwner())
                                .phoneNumber(request.getPhoneNumber())
                                .partnerAddress(request.getPartnerAddress())
                                .partnerEmail(request.getPartnerEmail())
                                .partnerAccount(request.getPartnerAccount())
                                .partnerBank(request.getPartnerBank())
                                .howToPay(request.getHowToPay())
                                .registDate(LocalDate.now())
                                .agreeServiceYn(Boolean.TRUE.equals(request.getAgreeService()) ? "Y" : "N")
                                .agreePrivacyYn(Boolean.TRUE.equals(request.getAgreePrivacy()) ? "Y" : "N")
                                .agreeMarketingYn(Boolean.TRUE.equals(request.getAgreeMarketing()) ? "Y" : "N")
                                .agreeAllYn(Boolean.TRUE.equals(request.getAgreeAll()) ? "Y" : "N")
                                .build();
                partnerRepository.save(partner);

                // 3. 권한 부여
                MemberRole role = MemberRole.builder()
                                .username(request.getUsername())
                                .roles("ROLE_PARTNER")
                                .build();
                memberRoleRepository.save(role);

                // 4. 자동 로그인
                return login(new AuthDto.LoginRequest(
                                request.getUsername(), request.getPassword()));
        }
}
