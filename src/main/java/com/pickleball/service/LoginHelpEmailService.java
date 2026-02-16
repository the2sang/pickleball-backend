package com.pickleball.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHelpEmailService {

    private final MailService mailService;

    public void sendLoginFailureNotice(String toEmail, String username) {
        String subject = "[LESGO PiCKLE] 로그인 실패 안내";
        String body = """
                안녕하세요. LESGO PiCKLE 입니다.

                입력하신 계정(%s)으로 로그인 실패가 5회 감지되어
                계정이 10분간 잠금 처리되었습니다.

                본인이 시도한 것이 아니라면 비밀번호 변경을 권장합니다.

                - 계정 ID: %s

                감사합니다.
                """.formatted(username, username);

        mailService.send(toEmail, subject, body);
    }
}
