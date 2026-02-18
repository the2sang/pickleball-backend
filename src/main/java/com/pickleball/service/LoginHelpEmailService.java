package com.pickleball.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHelpEmailService {

    private final MailService mailService;

    @Value("${app.auth.password-reset-url:http://localhost:5173/find-password}")
    private String passwordResetUrl;

    public void sendLoginFailureNotice(String toEmail, String username) {
        String subject = "[LESGO PiCKLE] 로그인 실패 안내";
        String body = """
                안녕하세요. LESGO PiCKLE 입니다.

                입력하신 계정(%s)으로 로그인 실패가 5회 감지되어
                계정이 10분간 잠금 처리되었습니다.

                아래 비밀번호 찾기 링크에서 등록된 이메일을 입력하면
                영문자+숫자 6자의 임시 비밀번호를 발급받을 수 있습니다.
                %s

                - 계정 ID: %s

                감사합니다.
                """.formatted(username, passwordResetUrl, username);

        mailService.send(toEmail, subject, body);
    }

    public void sendUsernameRecovery(String toEmail, String username) {
        String subject = "[LESGO PiCKLE] 아이디 찾기 안내";
        String body = """
                안녕하세요. LESGO PiCKLE 입니다.

                요청하신 계정 아이디는 아래와 같습니다.

                - 계정 ID: %s

                감사합니다.
                """.formatted(username);
        mailService.send(toEmail, subject, body);
    }

    public void sendTemporaryPassword(String toEmail, String username, String temporaryPassword) {
        String subject = "[LESGO PiCKLE] 임시 비밀번호 발급 안내";
        String body = """
                안녕하세요. LESGO PiCKLE 입니다.

                계정(%s)의 임시 비밀번호가 발급되었습니다.

                - 임시 비밀번호: %s

                로그인 후 반드시 비밀번호를 변경해주세요.

                감사합니다.
                """.formatted(username, temporaryPassword);

        mailService.send(toEmail, subject, body);
    }
}
