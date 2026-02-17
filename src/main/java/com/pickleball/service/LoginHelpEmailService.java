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
                안녕하세요 LESGO 입니다.

                입력하신 계정(%s)으로 로그인 실패가 5회 감지되어
                계정이 10분간 잠금 처리되었습니다.

                본인이 시도한 것이 아니라면 비밀번호 변경을 권장합니다.

                - 계정 ID: %s

                감사합니다.
                """.formatted(username, username);

        mailService.send(toEmail, subject, body);
    }

    public void sendFindIdEmail(String toEmail, String username) {
        String subject = "[LESGO PiCKLE] 아이디 찾기 안내";
        String body = """
                안녕하세요 LESGO 입니다.

                요청하신 아이디 찾기 결과를 안내드립니다.

                - 가입 아이디: %s

                본인이 요청하지 않았다면 본 메일을 무시하시고
                계정 보안을 위해 비밀번호를 변경해 주세요.

                감사합니다.
                """.formatted(username);
        mailService.send(toEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String username, String temporaryPassword) {
        String subject = "[LESGO PiCKLE] 비밀번호 초기화 안내";
        String body = """
                안녕하세요 LESGO 입니다.

                요청하신 비밀번호 초기화가 완료되었습니다.
                아래 임시 비밀번호로 로그인 후 비밀번호를 변경해 주세요.

                - 가입 아이디: %s
                - 임시 비밀번호: %s

                본인이 요청하지 않았다면 즉시 고객센터에 문의해 주세요.

                감사합니다.
                """.formatted(username, temporaryPassword);
        mailService.send(toEmail, subject, body);
    }
}
