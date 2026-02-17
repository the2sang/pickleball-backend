package com.pickleball.service;

import com.pickleball.entity.LoginFailure;
import com.pickleball.repository.LoginFailureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginFailureService {

    private final LoginFailureRepository loginFailureRepository;

    @Value("${app.security.login.failure-threshold:5}")
    private int failureThreshold;

    @Value("${app.security.login.lock-minutes:10}")
    private int lockMinutes;

    @Transactional(readOnly = true)
    public boolean isLocked(String username) {
        return loginFailureRepository.findById(normalize(username))
                .map(LoginFailure::getLockedUntil)
                .map(until -> until != null && until.isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public long remainingLockSeconds(String username) {
        return loginFailureRepository.findById(normalize(username))
                .map(LoginFailure::getLockedUntil)
                .map(until -> until == null ? 0L : ChronoUnit.SECONDS.between(LocalDateTime.now(), until))
                .map(sec -> Math.max(sec, 0L))
                .orElse(0L);
    }

    @Transactional
    public int recordFailure(String username) {
        String key = normalize(username);
        LocalDateTime now = LocalDateTime.now();

        LoginFailure entity = loginFailureRepository.findById(key)
                .orElseGet(() -> LoginFailure.builder().username(key).failCount(0).build());

        // If currently locked, do not increase count.
        if (entity.getLockedUntil() != null && entity.getLockedUntil().isAfter(now)) {
            return entity.getFailCount();
        }

        entity.setFailCount(entity.getFailCount() + 1);
        entity.setLastFailedAt(now);

        if (entity.getFailCount() >= failureThreshold) {
            // lock only when crossing threshold
            if (entity.getLockedUntil() == null || !entity.getLockedUntil().isAfter(now)) {
                entity.setLockedUntil(now.plusMinutes(lockMinutes));
            }
        }

        loginFailureRepository.save(entity);
        return entity.getFailCount();
    }

    @Transactional(readOnly = true)
    public Optional<LoginFailure> get(String username) {
        return loginFailureRepository.findById(normalize(username));
    }

    @Transactional(readOnly = true)
    public int getFailCount(String username) {
        return loginFailureRepository.findById(normalize(username))
                .map(LoginFailure::getFailCount)
                .orElse(0);
    }

    @Transactional
    public void markNotified(String username) {
        loginFailureRepository.findById(normalize(username)).ifPresent(entity -> {
            entity.setLastNotifiedAt(LocalDateTime.now());
            loginFailureRepository.save(entity);
        });
    }

    @Transactional
    public void reset(String username) {
        String key = normalize(username);
        loginFailureRepository.findById(key).ifPresent(entity -> {
            entity.setFailCount(0);
            entity.setLastFailedAt(LocalDateTime.now());
            entity.setLastNotifiedAt(null);
            entity.setLockedUntil(null);
            loginFailureRepository.save(entity);
        });
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim();
    }
}
