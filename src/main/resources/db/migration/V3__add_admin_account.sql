-- =============================================
-- V3: 시스템 관리자 계정 시드 데이터
-- =============================================

-- admin 계정 생성 (비밀번호: admin1234 → BCrypt 해시)
INSERT INTO account (username, password, account_type, name, create_date)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', '시스템관리자', NOW())
ON CONFLICT (username) DO NOTHING;

-- admin 권한 부여
INSERT INTO member_role (username, roles)
VALUES ('admin', 'ROLE_ADMIN')
ON CONFLICT DO NOTHING;
