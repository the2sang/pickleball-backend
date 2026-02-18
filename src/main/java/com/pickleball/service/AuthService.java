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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
        private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
        private static final String DIGITS = "0123456789";
        private static final String LOWERCASE_AND_DIGITS = LOWERCASE_LETTERS + DIGITS;
        private static final int TEMP_PASSWORD_LENGTH = 4;

        private final SecureRandom secureRandom = new SecureRandom();

        private final AuthenticationManager authenticationManager;
        private final AccountRepository accountRepository;
        private final MemberRepository memberRepository;
        private final MemberRoleRepository memberRoleRepository;
        private final PartnerRepository partnerRepository;
        private final JwtTokenProvider jwtTokenProvider;
        private final PasswordEncoder passwordEncoder;
        private final LoginFailureService loginFailureService;
        private final LoginHelpEmailService loginHelpEmailService;

        @Value("${app.security.login.failure-threshold:5}")
        private int failureThreshold;

        @Transactional(readOnly = true)
        public AuthDto.UsernameCheckResponse checkUsername(AuthDto.UsernameCheckRequest request) {
                boolean exists = accountRepository.existsByUsername(request.getUsername());
                if (exists) {
                        return AuthDto.UsernameCheckResponse.builder()
                                        .available(false)
                                        .message("이미 사용 중인 아이디입니다")
                                        .build();
                }

                return AuthDto.UsernameCheckResponse.builder()
                                .available(true)
                                .message("사용 가능한 아이디입니다")
                                .build();
        }

        public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
                // If locked, do not try authentication.
                if (loginFailureService.isLocked(request.getUsername())) {
                        throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
                }

                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getUsername(), request.getPassword()));

                        // 로그인 성공 시 실패 카운트 초기화
                        loginFailureService.reset(request.getUsername());

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
                } catch (BadCredentialsException e) {
                        String username = request.getUsername();
                        int failCount = loginFailureService.recordFailure(username);

                        if (failCount < failureThreshold) {
                                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                        }

                        // Threshold 도달 시: username 존재 여부로 안내 문구 분기
                        Account account = accountRepository.findByUsername(username).orElse(null);
                        if (account == null) {
                                // 보안상 실제 발송 여부와 무관하게 안내 문구는 동일하게 제공
                                throw new BusinessException(ErrorCode.LOGIN_HELP_SENT_FORGOT_ID);
                        }

                        String email = resolveAccountEmail(account);
                        if (failCount == failureThreshold && email != null && !email.isBlank()) {
                                loginHelpEmailService.sendLoginFailureNotice(email, account.getUsername());
                                loginFailureService.markNotified(username);
                        }

                throw new BusinessException(ErrorCode.LOGIN_HELP_SENT_PASSWORD);
                }
        }

        @Transactional(readOnly = true)
        public AuthDto.MessageResponse recoverUsername(AuthDto.UsernameRecoverRequest request) {
                String email = normalizeEmail(request.getEmail());
                findAccountByEmail(email).ifPresent(account ->
                                loginHelpEmailService.sendUsernameRecovery(email, account.getUsername()));

                return AuthDto.MessageResponse.builder()
                                .message("입력하신 이메일로 아이디 안내를 발송했습니다")
                                .build();
        }

        @Transactional
        public AuthDto.MessageResponse resetPassword(AuthDto.PasswordResetRequest request) {
                String email = normalizeEmail(request.getEmail());
                findAccountByEmail(email).ifPresent(account -> {
                        String temporaryPassword = generateTemporaryPassword();
                        account.setPassword(passwordEncoder.encode(temporaryPassword));
                        accountRepository.save(account);
                        loginFailureService.reset(account.getUsername());
                        loginHelpEmailService.sendTemporaryPassword(email, account.getUsername(), temporaryPassword);
                });

                return AuthDto.MessageResponse.builder()
                                .message("입력하신 이메일로 임시 비밀번호를 발송했습니다")
                                .build();
        }

        private String resolveAccountEmail(Account account) {
                if (account == null) {
                        return null;
                }

                String type = account.getAccountType() == null ? "" : account.getAccountType().trim().toUpperCase();
                if ("PARTNER".equals(type)) {
                        return partnerRepository.findByAccountId(account.getId())
                                        .map(p -> p.getPartnerEmail())
                                        .orElse(null);
                }

                // MEMBER or others
                return memberRepository.findByAccountId(account.getId())
                                .map(m -> m.getEmail())
                                .orElse(null);
        }

        private Optional<Account> findAccountByEmail(String email) {
                if (email == null || email.isBlank()) {
                        return Optional.empty();
                }

                Optional<Account> memberAccount = memberRepository.findByEmail(email)
                                .flatMap(member -> accountRepository.findById(member.getAccountId()));
                if (memberAccount.isPresent()) {
                        return memberAccount;
                }

                return partnerRepository.findByPartnerEmail(email)
                                .flatMap(partner -> accountRepository.findById(partner.getAccountId()));
        }

        private String normalizeEmail(String email) {
                return email == null ? "" : email.trim();
        }

        private String generateTemporaryPassword() {
                List<Character> chars = new ArrayList<>(TEMP_PASSWORD_LENGTH);
                chars.add(LOWERCASE_LETTERS.charAt(secureRandom.nextInt(LOWERCASE_LETTERS.length())));
                chars.add(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
                for (int i = 2; i < TEMP_PASSWORD_LENGTH; i++) {
                        chars.add(LOWERCASE_AND_DIGITS.charAt(secureRandom.nextInt(LOWERCASE_AND_DIGITS.length())));
                }
                Collections.shuffle(chars, secureRandom);

                StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
                for (Character ch : chars) {
                        sb.append(ch);
                }
                return sb.toString();
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
