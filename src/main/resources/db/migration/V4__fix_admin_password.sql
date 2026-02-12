-- =============================================
-- V4: admin 계정 비밀번호 수정 (해시 불일치 수정)
-- Password: admin1234
-- =============================================

UPDATE account
SET password = '$2a$10$mBHMCtnWe/As/3lFgjIWq.5ShQYYafrOoOPBwlZi7qtwhmlogrZ8i',
    update_date = NOW()
WHERE username = 'admin';
