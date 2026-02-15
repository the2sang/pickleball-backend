-- =============================================
-- 테스트 계정 생성 (user1 ~ user12)
-- =============================================
-- 패스워드: user1!123 (모든 계정 동일)
-- BCrypt 해시: $2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e

-- 1. Account 테이블에 계정 추가
INSERT INTO account (username, password, account_type, name, create_date, update_date) VALUES
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '김민수', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user2', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '이영희', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user3', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '박철수', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user4', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '백두산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user5', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '한라산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user6', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '지리산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user7', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '설악산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user8', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '북한산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user9', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '금강산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user10', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '속리산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user11', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '오대산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user12', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '태백산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 2. Member 테이블에 회원 정보 추가
INSERT INTO member (account_id, phone_number, name, nic_name, game_level, dupr_point, email, sex, regist_date, create_date, update_date)
SELECT
    a.id,
    '010-' || LPAD((ROW_NUMBER() OVER (ORDER BY a.username))::TEXT, 4, '0') || '-0001',
    a.name,
    a.name,  -- 닉네임은 이름과 동일
    CASE
        WHEN a.username IN ('user1', 'user4', 'user7', 'user10') THEN '초급'
        WHEN a.username IN ('user2', 'user5', 'user8', 'user11') THEN '중급'
        ELSE '상급'
    END,
    CASE
        WHEN a.username IN ('user1', 'user4', 'user7', 'user10') THEN '2.5'
        WHEN a.username IN ('user2', 'user5', 'user8', 'user11') THEN '3.5'
        ELSE '4.5'
    END,
    a.username || '@test.com',
    CASE WHEN CAST(SUBSTRING(a.username FROM 5) AS INTEGER) % 2 = 0 THEN '남' ELSE '여' END,
    CURRENT_DATE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account a
WHERE a.username IN ('user1', 'user2', 'user3', 'user4', 'user5', 'user6', 'user7', 'user8', 'user9', 'user10', 'user11', 'user12')
ON CONFLICT (account_id) DO NOTHING;

-- 3. Member Role 추가 (일반 사용자 권한)
INSERT INTO member_role (username, roles)
SELECT a.username, 'ROLE_USER'
FROM account a
WHERE a.username IN ('user1', 'user2', 'user3', 'user4', 'user5', 'user6', 'user7', 'user8', 'user9', 'user10', 'user11', 'user12')
ON CONFLICT DO NOTHING;

-- 완료 메시지 (로그용)
DO $$
BEGIN
    RAISE NOTICE '===========================================';
    RAISE NOTICE '테스트 계정 생성 완료 (user1 ~ user12)';
    RAISE NOTICE '===========================================';
    RAISE NOTICE '패스워드: user1!123 (모든 계정 동일)';
    RAISE NOTICE '';
    RAISE NOTICE '계정 목록:';
    RAISE NOTICE '- user1 (김민수) - 초급, DUPR 2.5, 여';
    RAISE NOTICE '- user2 (이영희) - 중급, DUPR 3.5, 남';
    RAISE NOTICE '- user3 (박철수) - 상급, DUPR 4.5, 여';
    RAISE NOTICE '- user4 (백두산) - 초급, DUPR 2.5, 남';
    RAISE NOTICE '- user5 (한라산) - 중급, DUPR 3.5, 여';
    RAISE NOTICE '- user6 (지리산) - 상급, DUPR 4.5, 남';
    RAISE NOTICE '- user7 (설악산) - 초급, DUPR 2.5, 여';
    RAISE NOTICE '- user8 (북한산) - 중급, DUPR 3.5, 남';
    RAISE NOTICE '- user9 (금강산) - 상급, DUPR 4.5, 여';
    RAISE NOTICE '- user10 (속리산) - 초급, DUPR 2.5, 남';
    RAISE NOTICE '- user11 (오대산) - 중급, DUPR 3.5, 여';
    RAISE NOTICE '- user12 (태백산) - 상급, DUPR 4.5, 남';
    RAISE NOTICE '===========================================';
END $$;
