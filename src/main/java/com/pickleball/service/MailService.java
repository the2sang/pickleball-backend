package com.pickleball.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.from:no-reply@lesgo-pickle.local}")
    private String from;

    public void send(String to, String subject, String body) {
        if (!enabled) {
            log.info("Mail disabled. Skip sending to={} subject={}", to, subject);
            return;
        }

        if (to == null || to.isBlank()) {
            log.warn("Mail recipient is blank. Skip sending subject={}", subject);
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send email to={} subject={}: {}", to, subject, e.getMessage());
        }
    }
}
