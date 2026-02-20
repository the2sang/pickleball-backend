-- =============================================
-- V7: 용인 카이로스 피클볼 클럽 및 예약 데이터 생성
-- =============================================

-- 1. user1, user2, user3 계정 생성 (아직 없는 경우)
-- 패스워드: user1!123 (BCrypt 해시)
INSERT INTO account (username, password, account_type, name, create_date, update_date)
VALUES
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '김민수', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user2', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '이영희', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user3', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'USER', '박철수', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 2. user1, user2, user3 Member 정보 추가
INSERT INTO member (account_id, phone_number, name, nic_name, game_level, dupr_point, email, sex, regist_date, create_date, update_date)
SELECT
    a.id,
    '010-100' || SUBSTRING(a.username FROM 5) || '-0001',
    a.name,
    a.name,
    CASE
        WHEN a.username = 'user1' THEN '중급'
        WHEN a.username = 'user2' THEN '초급'
        ELSE '상급'
    END,
    CASE
        WHEN a.username = 'user1' THEN '3.0'
        WHEN a.username = 'user2' THEN '2.8'
        ELSE '4.2'
    END,
    a.username || '@test.com',
    CASE WHEN a.username = 'user2' THEN '여' ELSE '남' END,
    CURRENT_DATE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account a
WHERE a.username IN ('user1', 'user2', 'user3')
ON CONFLICT (account_id) DO NOTHING;

-- 3. user1, user2, user3 Member Role 추가
INSERT INTO member_role (username, roles)
SELECT a.username, 'ROLE_USER'
FROM account a
WHERE a.username IN ('user1', 'user2', 'user3')
ON CONFLICT DO NOTHING;

-- 4. 용인 카이로스 피클볼 클럽 파트너 계정 생성
-- 패스워드: kairos!123
INSERT INTO account (username, password, account_type, name, create_date, update_date)
VALUES ('kairos', '$2a$10$KX7pZ9sYYqJ4wZzJ4uLOickgx2ZMRZoMye8Kw9Jq7r6Hq1F9H3Jy5e', 'PARTNER', '카이로스', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 5. 용인 카이로스 피클볼 클럽 파트너 정보 등록
INSERT INTO partner (business_partner, owner, phone_number, partner_address, partner_level, partner_email, regist_date, account_id, create_date, update_date)
SELECT
    '용인 카이로스 피클볼 클럽',
    '카이로스',
    '031-1234-5678',
    '경기도 용인시 처인구 백암면',
    '1',
    'kairos@test.com',
    CURRENT_DATE,
    a.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM account a
WHERE a.username = 'kairos'
AND NOT EXISTS (SELECT 1 FROM partner WHERE business_partner = '용인 카이로스 피클볼 클럽');

-- 6. 파트너 권한 추가
INSERT INTO member_role (username, roles)
SELECT 'kairos', 'ROLE_PARTNER'
WHERE NOT EXISTS (SELECT 1 FROM member_role WHERE username = 'kairos' AND roles = 'ROLE_PARTNER');

-- 7. 두나미스 코트 생성
INSERT INTO court (partner_id, court_name, personnel_number, court_level, reserv_close, game_time, create_date, update_date)
SELECT
    p.id,
    '두나미스',
    8,
    '중급',
    'N',
    '06:00 ~ 22:00',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM partner p
WHERE p.business_partner = '용인 카이로스 피클볼 클럽'
AND NOT EXISTS (SELECT 1 FROM court c WHERE c.partner_id = p.id AND c.court_name = '두나미스');

-- 8. 오늘 날짜의 06:00~09:00 시간대 스케줄 생성 (OPEN_GAME)
INSERT INTO court_schedule (court_id, game_date, start_time, end_time, schedule_type)
SELECT
    c.id,
    CURRENT_DATE,
    '06:00'::TIME,
    '09:00'::TIME,
    'OPEN_GAME'
FROM court c
INNER JOIN partner p ON c.partner_id = p.id
WHERE p.business_partner = '용인 카이로스 피클볼 클럽'
AND c.court_name = '두나미스'
AND NOT EXISTS (
    SELECT 1 FROM court_schedule cs
    WHERE cs.court_id = c.id
    AND cs.game_date = CURRENT_DATE
    AND cs.start_time = '06:00'::TIME
);

-- 9. user2~user10 예약 등록 (06:00~09:00 시간대)
-- 예약 순서대로 생성 (create_date가 예약 순서를 결정)
INSERT INTO reservation (court_id, username, cancel_yn, time_name, game_date, time_slot, reserv_type, create_date, update_date)
SELECT
    c.id,
    u.username,
    'N',
    '오픈게임',
    CURRENT_DATE,
    '06:00~09:00',
    '0',
    CURRENT_TIMESTAMP + (u.order_num || ' seconds')::INTERVAL,
    CURRENT_TIMESTAMP
FROM court c
INNER JOIN partner p ON c.partner_id = p.id
CROSS JOIN (
    VALUES
        ('user2', 1),
        ('user3', 2),
        ('user4', 3),
        ('user5', 4),
        ('user6', 5),
        ('user7', 6),
        ('user8', 7),
        ('user9', 8),
        ('user10', 9)
) AS u(username, order_num)
WHERE p.business_partner = '용인 카이로스 피클볼 클럽'
AND c.court_name = '두나미스'
AND NOT EXISTS (
    SELECT 1 FROM reservation r
    WHERE r.court_id = c.id
    AND r.username = u.username
    AND r.game_date = CURRENT_DATE
    AND r.time_slot = '06:00~09:00'
);

-- 완료 메시지 (로그용)
DO $$
BEGIN
    RAISE NOTICE '용인 카이로스 피클볼 클럽 데이터 생성 완료';
    RAISE NOTICE '- 파트너: 용인 카이로스 피클볼 클럽';
    RAISE NOTICE '- 코트: 두나미스 (정원 8명)';
    RAISE NOTICE '- 예약: user2~user10 (총 9명) - 06:00~09:00';
    RAISE NOTICE '- 날짜: %', CURRENT_DATE;
END $$;
