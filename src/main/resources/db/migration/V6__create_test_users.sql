-- 테스트 계정 생성 (user4 ~ user10)
-- 패스워드: user1!123
-- 닉네임: 한국의 대표 명산

-- Account 테이블에 계정 추가
-- 주의: 패스워드는 BCrypt로 인코딩된 값입니다
INSERT INTO account (username, password, name, phone_number, email, account_type, create_date, update_date) VALUES
('user4', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', '백두산', '010-1004-0001', 'user4@test.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user5', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', '한라산', '010-1005-0001', 'user5@test.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user6', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', '지리산', '010-1006-0001', 'user6@test.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user7', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', '설악산', '010-1007-0001', 'user7@test.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user8', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', '북한산', '010-1008-0001', 'user8@test.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user9', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', '금강산', '010-1009-0001', 'user9@test.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user10', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', '속리산', '010-1010-0001', 'user10@test.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Member 테이블에 회원 정보 추가
INSERT INTO member (account_id, nic_name, game_level, dupr_point, sex, birth_date, address, create_date, update_date)
SELECT 
    a.id,
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
    CASE WHEN MOD(CAST(SUBSTRING(a.username, 5) AS INTEGER), 2) = 0 THEN '남' ELSE '여' END,
    DATE '1990-01-01',
    '서울시 강남구',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account a
WHERE a.username IN ('user4', 'user5', 'user6', 'user7', 'user8', 'user9', 'user10');

-- Member Role 추가 (일반 사용자)
INSERT INTO member_role (account_id, role_name)
SELECT a.id, 'ROLE_USER'
FROM account a
WHERE a.username IN ('user4', 'user5', 'user6', 'user7', 'user8', 'user9', 'user10');
