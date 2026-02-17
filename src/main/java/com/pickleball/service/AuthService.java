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
                String username = normalize(request.getUsername());

                // If locked, do not try authentication.
                if (loginFailureService.isLocked(username)) {
                        throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
                }

                Account account = accountRepository.findByUsername(username).orElse(null);
                if (account == null) {
                        throw new BusinessException(ErrorCode.USERNAME_NOT_REGISTERED);
                }

                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        username, request.getPassword()));

                        // 로그인 성공 시 실패 카운트 초기화
                        loginFailureService.reset(username);

                        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

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
                        int failCount = loginFailureService.recordFailure(username);

                        if (failCount < failureThreshold) {
                                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                        }

                        String email = resolveAccountEmail(account);
                        if (failCount == failureThreshold && email != null && !email.isBlank()) {
                                loginHelpEmailService.sendLoginFailureNotice(email, account.getUsername());
                                loginFailureService.markNotified(username);
                        }

                        throw new BusinessException(ErrorCode.LOGIN_HELP_SENT_PASSWORD);
                }
        }

        @Transactional
        public AuthDto.MessageResponse findId(AuthDto.FindIdRequest request) {
                String email = normalizeEmail(request.getEmail());
                if (email == null) {
                        throw new BusinessException(ErrorCode.REGISTERED_EMAIL_MISMATCH);
                }

                String username = memberRepository.findByEmailIgnoreCase(email)
                                .map(member -> accountRepository.findById(member.getAccountId()).orElse(null))
                                .map(Account::getUsername)
                                .orElseGet(() -> partnerRepository.findByPartnerEmailIgnoreCase(email)
                                                .map(partner -> accountRepository.findById(partner.getAccountId()).orElse(null))
                                                .map(Account::getUsername)
                                                .orElse(null));

                if (username == null || username.isBlank()) {
                        throw new BusinessException(ErrorCode.REGISTERED_EMAIL_MISMATCH);
                }

                loginHelpEmailService.sendFindIdEmail(email, username);
                return AuthDto.MessageResponse.builder()
                                .code("FIND_ID_MAIL_SENT")
                                .message("등록된 이메일로 아이디 안내 메일을 발송했습니다.")
                                .build();
        }

        @Transactional
        public AuthDto.MessageResponse resetPassword(AuthDto.ResetPasswordRequest request) {
                String username = normalize(request.getUsername());
                String email = normalizeEmail(request.getEmail());

                Account account = accountRepository.findByUsername(username)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USERNAME_NOT_REGISTERED));

                if (email == null) {
                        throw new BusinessException(ErrorCode.REGISTERED_EMAIL_MISMATCH);
                }

                int failCount = loginFailureService.getFailCount(username);
                if (failCount < failureThreshold) {
                        throw new BusinessException(ErrorCode.PASSWORD_RESET_NOT_ALLOWED);
                }

                String registeredEmail = normalizeEmail(resolveAccountEmail(account));
                if (registeredEmail == null || !registeredEmail.equalsIgnoreCase(email)) {
                        throw new BusinessException(ErrorCode.REGISTERED_EMAIL_MISMATCH);
                }

                String temporaryPassword = generateTemporaryPassword(6);
                account.setPassword(passwordEncoder.encode(temporaryPassword));
                accountRepository.save(account);
                loginFailureService.reset(username);
                loginHelpEmailService.sendPasswordResetEmail(email, username, temporaryPassword);

                return AuthDto.MessageResponse.builder()
                                .code("PASSWORD_RESET_MAIL_SENT")
                                .message("등록된 이메일로 초기화된 비밀번호를 발송했습니다.")
                                .build();
        }

        private String resolveAccountEmail(Account account) {
                if (account == null) {
                        return null;
                }

                // account_type 값 불일치/누락 데이터가 있어도 조회되도록 양쪽을 모두 확인
                String partnerEmail = partnerRepository.findByAccountId(account.getId())
                                .map(Partner::getPartnerEmail)
                                .orElse(null);
                if (partnerEmail != null && !partnerEmail.isBlank()) {
                        return partnerEmail;
                }

                return memberRepository.findByAccountId(account.getId())
                                .map(Member::getEmail)
                                .orElse(null);
        }

        private String normalize(String username) {
                return username == null ? "" : username.trim();
        }

        private String normalizeEmail(String email) {
                if (email == null) {
                        return null;
                }
                String value = email.trim();
                return value.isBlank() ? null : value.toLowerCase();
        }

        private String generateTemporaryPassword(int length) {
                final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                final String lower = "abcdefghijklmnopqrstuvwxyz";
                final String digits = "0123456789";
                final String all = upper + lower + digits;
                SecureRandom random = new SecureRandom();

                char[] password = new char[length];
                password[0] = upper.charAt(random.nextInt(upper.length()));
                password[1] = lower.charAt(random.nextInt(lower.length()));
                password[2] = digits.charAt(random.nextInt(digits.length()));
                for (int i = 3; i < length; i++) {
                        password[i] = all.charAt(random.nextInt(all.length()));
                }

                for (int i = password.length - 1; i > 0; i--) {
                        int j = random.nextInt(i + 1);
                        char tmp = password[i];
                        password[i] = password[j];
                        password[j] = tmp;
                }

                return new String(password);
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
