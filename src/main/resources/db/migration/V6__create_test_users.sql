-- 테스트 계정 생성 (user4 ~ user10)
-- 패스워드: user1!123
-- 닉네임: 한국의 대표 명산

-- Account 테이블에 계정 추가
-- 주의: 패스워드는 BCrypt로 인코딩된 값입니다
INSERT INTO account (username, password, account_type, name, create_date, update_date) VALUES
('user4', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '백두산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user5', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '한라산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user6', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '지리산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user7', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '설악산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user8', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '북한산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user9', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '금강산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user10', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '속리산', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Member 테이블에 회원 정보 추가
INSERT INTO member (account_id, phone_number, name, nic_name, game_level, dupr_point, email, sex, regist_date, create_date, update_date)
SELECT
    a.id,
    '010-10' || SUBSTRING(a.username FROM 5 FOR 2) || '-0001',
    a.name,
    a.name,
    CASE
        WHEN a.username IN ('user4', 'user7') THEN '초급'
        WHEN a.username IN ('user5', 'user8') THEN '중급'
        ELSE '상급'
    END,
    CASE
        WHEN a.username IN ('user4', 'user7') THEN '2.5'
        WHEN a.username IN ('user5', 'user8') THEN '3.5'
        ELSE '4.5'
    END,
    a.username || '@test.com',
    CASE WHEN MOD(CAST(SUBSTRING(a.username FROM 5) AS INTEGER), 2) = 0 THEN '남' ELSE '여' END,
    CURRENT_DATE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account a
WHERE a.username IN ('user4', 'user5', 'user6', 'user7', 'user8', 'user9', 'user10');

-- Member Role 추가 (일반 사용자)
INSERT INTO member_role (username, roles)
SELECT a.username, 'ROLE_USER'
FROM account a
WHERE a.username IN ('user4', 'user5', 'user6', 'user7', 'user8', 'user9', 'user10');
