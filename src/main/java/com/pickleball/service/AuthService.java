package com.pickleball.service;

import com.pickleball.dto.AuthDto;
import com.pickleball.entity.Member;
import com.pickleball.entity.MemberRole;
import com.pickleball.exception.BusinessException;
import com.pickleball.exception.BusinessException.ErrorCode;
import com.pickleball.repository.MemberRepository;
import com.pickleball.repository.MemberRoleRepository;
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
    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberRepository.findByUsername(request.getUsername())
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
                .username(member.getUsername())
                .name(member.getName())
                .roles(roles)
                .build();
    }

    @Transactional
    public AuthDto.TokenResponse signup(AuthDto.SignupRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .sex(request.getSex())
                .ageRange(request.getAgeRange())
                .location(request.getLocation())
                .gameLevel(request.getGameLevel() != null ? request.getGameLevel() : "입문")
                .memberLevel("정회원")
                .registDate(LocalDate.now())
                .build();
        memberRepository.save(member);

        MemberRole role = MemberRole.builder()
                .username(request.getUsername())
                .roles("ROLE_USER")
                .build();
        memberRoleRepository.save(role);

        // 자동 로그인
        return login(new AuthDto.LoginRequest(
                request.getUsername(), request.getPassword()));
    }
}
