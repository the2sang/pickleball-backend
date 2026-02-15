-- =============================================
-- 피클볼 예약 관리 시스템
-- PostgreSQL 스키마 + 초기 데이터
--
-- Database : pickleball_db
-- Schema   : pickleball
-- Generated: 2026-02-12
-- =============================================

-- =============================================
-- 1. 스키마 생성
-- =============================================
CREATE SCHEMA IF NOT EXISTS pickleball;
SET search_path TO pickleball;


-- =============================================
-- 2. 테이블 생성 (DDL)
-- =============================================

-- -----------------------------------------
-- 2-1. account (통합 계정)
-- -----------------------------------------
CREATE TABLE account (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(20)     NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    account_type    VARCHAR(10)     NOT NULL DEFAULT 'MEMBER',
    name            VARCHAR(50)     NOT NULL,
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP
);

COMMENT ON TABLE  account IS '통합 계정 정보';
COMMENT ON COLUMN account.account_type IS 'MEMBER(일반회원), PARTNER(사업장), ADMIN(관리자)';

CREATE INDEX idx_account_username ON account(username);
CREATE INDEX idx_account_type     ON account(account_type);

-- -----------------------------------------
-- 2-2. member (일반 회원 상세)
-- -----------------------------------------
CREATE TABLE member (
    id              BIGSERIAL       PRIMARY KEY,
    account_id      BIGINT          NOT NULL UNIQUE,
    phone_number    VARCHAR(20)     NOT NULL,
    name            VARCHAR(50)     NOT NULL,
    nic_name        VARCHAR(100),
    age_range       VARCHAR(10),
    location        VARCHAR(20),
    circle_name     VARCHAR(50),
    game_level      VARCHAR(10)     NOT NULL DEFAULT '입문',
    dupr_point      VARCHAR(10),
    email           VARCHAR(50),
    member_level    VARCHAR(10)     NOT NULL DEFAULT '정회원',
    sex             VARCHAR(10),
    regist_date     DATE            NOT NULL,
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,

    CONSTRAINT fk_member_account
        FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);

COMMENT ON TABLE  member IS '회원 상세 정보';
COMMENT ON COLUMN member.game_level   IS '입문/초급/초중급/중급/중상급/상급/전문가';
COMMENT ON COLUMN member.member_level IS '정회원/게스트/정지회원';

-- -----------------------------------------
-- 2-3. member_role (회원 권한)
-- -----------------------------------------
CREATE TABLE member_role (
    username        VARCHAR(20)     NOT NULL,
    roles           VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,

    PRIMARY KEY (username, roles),
    CONSTRAINT fk_member_role_account
        FOREIGN KEY (username) REFERENCES account(username) ON DELETE CASCADE
);

COMMENT ON TABLE member_role IS '회원 권한 (ROLE_USER, ROLE_PARTNER, ROLE_ADMIN)';

-- -----------------------------------------
-- 2-4. partner (사업장)
-- -----------------------------------------
CREATE TABLE partner (
    id              BIGSERIAL       PRIMARY KEY,
    account_id      BIGINT          UNIQUE,
    business_partner VARCHAR(100)   NOT NULL,
    owner           VARCHAR(100)    NOT NULL,
    phone_number    VARCHAR(20)     NOT NULL,
    partner_address VARCHAR(200)    NOT NULL,
    partner_level   VARCHAR(10)     NOT NULL DEFAULT '0',
    partner_email   VARCHAR(100)    NOT NULL,
    regist_date     DATE            NOT NULL,
    partner_account VARCHAR(50),
    partner_bank    VARCHAR(100),
    how_to_pay      VARCHAR(10),
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,

    CONSTRAINT fk_partner_account
        FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);

COMMENT ON TABLE  partner IS '사업장 정보';
COMMENT ON COLUMN partner.partner_level IS '예비업체(0), 정식등록업체(1)';
COMMENT ON COLUMN partner.how_to_pay   IS '예약건별 수수료(0), 월구독(1), 년구독(2)';

-- -----------------------------------------
-- 2-5. court (코트)
-- -----------------------------------------
CREATE TABLE court (
    id              BIGSERIAL       PRIMARY KEY,
    partner_id      BIGINT          NOT NULL,
    court_name      VARCHAR(50)     NOT NULL,
    personnel_number SMALLINT       DEFAULT 6,
    court_level     VARCHAR(10),
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,
    reserv_close    VARCHAR(2)      DEFAULT 'N',
    court_gugun     VARCHAR(2)      NOT NULL DEFAULT '01',
    game_time       VARCHAR(20),
    game_date       DATE,

    CONSTRAINT fk_court_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE CASCADE
);

COMMENT ON TABLE  court IS '코트 정보';
COMMENT ON COLUMN court.court_level  IS '초급/중급/상급';
COMMENT ON COLUMN court.reserv_close IS '예약마감 여부 (Y/N)';
COMMENT ON COLUMN court.court_gugun  IS '코트 성격 구분';

CREATE INDEX idx_court_partner ON court(partner_id);
CREATE INDEX idx_court_date    ON court(game_date);

-- -----------------------------------------
-- 2-6. court_schedule (코트 스케줄)
-- -----------------------------------------
CREATE TABLE court_schedule (
    id              BIGSERIAL       PRIMARY KEY,
    court_id        BIGINT          NOT NULL,
    game_date       DATE            NOT NULL,
    start_time      TIME            NOT NULL,
    end_time        TIME            NOT NULL,
    schedule_type   VARCHAR(20)     NOT NULL,

    CONSTRAINT fk_court_schedule_court
        FOREIGN KEY (court_id) REFERENCES court(id) ON DELETE CASCADE,
    CONSTRAINT uq_court_schedule_slot
        UNIQUE (court_id, game_date, start_time)
);

COMMENT ON TABLE  court_schedule IS '코트 시간대 스케줄';
COMMENT ON COLUMN court_schedule.schedule_type IS 'OPEN_GAME(오픈게임), RENTAL(대관)';

-- -----------------------------------------
-- 2-7. reservation (예약)
-- -----------------------------------------
CREATE TABLE reservation (
    id              BIGSERIAL       PRIMARY KEY,
    court_id        BIGINT          NOT NULL,
    username        VARCHAR(20)     NOT NULL,
    cancel_yn       VARCHAR(1)      DEFAULT 'N',
    time_name       VARCHAR(50)     NOT NULL DEFAULT '일반예약',
    game_date       DATE            NOT NULL,
    time_slot       VARCHAR(20)     NOT NULL,
    reserv_type     VARCHAR(10)     DEFAULT '0',
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,

    CONSTRAINT fk_reservation_court
        FOREIGN KEY (court_id) REFERENCES court(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_account
        FOREIGN KEY (username) REFERENCES account(username)
);

COMMENT ON TABLE  reservation IS '예약 정보';
COMMENT ON COLUMN reservation.cancel_yn   IS '취소 여부 (Y/N, soft delete)';
COMMENT ON COLUMN reservation.reserv_type IS '일반예약(0), 특별예약(1)';

CREATE INDEX idx_reservation_court_date ON reservation(court_id, game_date, time_slot);
CREATE INDEX idx_reservation_username   ON reservation(username);

-- -----------------------------------------
-- 2-8. game_result (게임 결과)
-- -----------------------------------------
CREATE TABLE game_result (
    id              BIGSERIAL       PRIMARY KEY,
    court_id        BIGINT          NOT NULL,
    game_result     VARCHAR(20),
    game_end        VARCHAR(2)      DEFAULT 'N',
    game_date       DATE,
    time_slot       VARCHAR(20),
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,

    CONSTRAINT fk_game_result_court
        FOREIGN KEY (court_id) REFERENCES court(id) ON DELETE CASCADE
);

COMMENT ON TABLE game_result IS '게임 결과';

-- -----------------------------------------
-- 2-9. member_suspension (회원 정지)
-- -----------------------------------------
CREATE TABLE member_suspension (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(20)     NOT NULL,
    partner_id      BIGINT          NOT NULL,
    suspend_type    VARCHAR(10)     NOT NULL,
    suspend_start   DATE            NOT NULL,
    suspend_end     DATE            NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_suspension_account
        FOREIGN KEY (username) REFERENCES account(username),
    CONSTRAINT fk_suspension_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id)
);

COMMENT ON TABLE  member_suspension IS '회원 정지 정보 (사업장별)';
COMMENT ON COLUMN member_suspension.suspend_type IS '1주/2주/3주/1개월/2개월/3개월/영구';

CREATE INDEX idx_suspension_lookup ON member_suspension(username, partner_id, is_active);

-- -----------------------------------------
-- 2-10. reject_vote (거부 투표)
-- -----------------------------------------
CREATE TABLE reject_vote (
    id              BIGSERIAL       PRIMARY KEY,
    court_id        BIGINT          NOT NULL,
    time_slot       VARCHAR(20)     NOT NULL,
    game_date       DATE            NOT NULL,
    target_user     VARCHAR(20)     NOT NULL,
    voter_user      VARCHAR(20)     NOT NULL,
    is_reject       BOOLEAN         NOT NULL DEFAULT TRUE,
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reject_vote_court
        FOREIGN KEY (court_id) REFERENCES court(id),
    CONSTRAINT fk_reject_vote_target
        FOREIGN KEY (target_user) REFERENCES account(username),
    CONSTRAINT fk_reject_vote_voter
        FOREIGN KEY (voter_user) REFERENCES account(username),
    CONSTRAINT uq_reject_vote
        UNIQUE (court_id, time_slot, game_date, target_user, voter_user)
);

COMMENT ON TABLE reject_vote IS '게임 참가 거부 투표';


-- =============================================
-- 3. 초기 데이터 (DML)
-- =============================================

-- -----------------------------------------
-- 3-1. 시스템 관리자 계정
--   username: admin / password: admin1234
-- -----------------------------------------
INSERT INTO account (username, password, account_type, name, create_date) VALUES
('admin', '$2a$10$mBHMCtnWe/As/3lFgjIWq.5ShQYYafrOoOPBwlZi7qtwhmlogrZ8i', 'ADMIN', '시스템관리자', NOW());

INSERT INTO member_role (username, roles, create_date) VALUES
('admin', 'ROLE_ADMIN', NOW());

-- -----------------------------------------
-- 3-2. 파트너(사업장) 계정
--   username: admin1, admin2, admin3
--   password: admin1!123
--   BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e
-- -----------------------------------------
INSERT INTO account (username, password, account_type, name, create_date) VALUES
('admin1', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'PARTNER', '김사업', NOW()),
('admin2', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'PARTNER', '이사업', NOW()),
('admin3', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'PARTNER', '박사업', NOW());

INSERT INTO member_role (username, roles, create_date) VALUES
('admin1', 'ROLE_PARTNER', NOW()),
('admin2', 'ROLE_PARTNER', NOW()),
('admin3', 'ROLE_PARTNER', NOW());

INSERT INTO partner (account_id, business_partner, owner, phone_number, partner_address, partner_level, partner_email, regist_date, create_date) VALUES
((SELECT id FROM account WHERE username = 'admin1'), '제로제로투 피클볼', '김사업', '010-1001-0001', '서울시 송파구 올림픽로 300', '1', 'admin1@pickle.com', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'admin2'), '강남 피클볼 클럽',   '이사업', '010-1002-0001', '서울시 강남구 테헤란로 100', '1', 'admin2@pickle.com', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'admin3'), '잠실 피클볼 센터',   '박사업', '010-1003-0001', '서울시 송파구 잠실로 200',   '1', 'admin3@pickle.com', CURRENT_DATE, NOW());

-- -----------------------------------------
-- 3-3. 일반 회원 계정 (user1 ~ user3)
--   password: user1!123
-- -----------------------------------------
INSERT INTO account (username, password, account_type, name, create_date) VALUES
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '박민수', NOW()),
('user2', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '김영희', NOW()),
('user3', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '이철수', NOW());

INSERT INTO member_role (username, roles, create_date) VALUES
('user1', 'ROLE_USER', NOW()),
('user2', 'ROLE_USER', NOW()),
('user3', 'ROLE_USER', NOW());

INSERT INTO member (account_id, phone_number, name, nic_name, game_level, dupr_point, sex, regist_date, create_date) VALUES
((SELECT id FROM account WHERE username = 'user1'), '010-2001-0001', '박민수', '박민수', '중급',   '3.5', '남성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user2'), '010-2002-0001', '김영희', '김영희', '초급',   '2.5', '여성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user3'), '010-2003-0001', '이철수', '이철수', '상급',   '4.5', '남성', CURRENT_DATE, NOW());

-- -----------------------------------------
-- 3-4. 테스트 회원 계정 (user4 ~ user10)
--   password: user1!123
--   닉네임: 한국 명산
-- -----------------------------------------
INSERT INTO account (username, password, account_type, name, create_date) VALUES
('user4',  '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '백두산', NOW()),
('user5',  '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '한라산', NOW()),
('user6',  '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '지리산', NOW()),
('user7',  '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '설악산', NOW()),
('user8',  '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '북한산', NOW()),
('user9',  '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '금강산', NOW()),
('user10', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JWB0Ql4Cir3Df8Jq7r6Hq1F9H3Jy5e', 'MEMBER', '속리산', NOW());

INSERT INTO member_role (username, roles, create_date) VALUES
('user4',  'ROLE_USER', NOW()),
('user5',  'ROLE_USER', NOW()),
('user6',  'ROLE_USER', NOW()),
('user7',  'ROLE_USER', NOW()),
('user8',  'ROLE_USER', NOW()),
('user9',  'ROLE_USER', NOW()),
('user10', 'ROLE_USER', NOW());

INSERT INTO member (account_id, phone_number, name, nic_name, game_level, dupr_point, sex, regist_date, create_date) VALUES
((SELECT id FROM account WHERE username = 'user4'),  '010-1004-0001', '백두산', '백두산', '초급', '2.5', '남성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user5'),  '010-1005-0001', '한라산', '한라산', '중급', '3.5', '여성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user6'),  '010-1006-0001', '지리산', '지리산', '상급', '4.5', '남성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user7'),  '010-1007-0001', '설악산', '설악산', '초급', '2.5', '여성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user8'),  '010-1008-0001', '북한산', '북한산', '중급', '3.5', '남성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user9'),  '010-1009-0001', '금강산', '금강산', '상급', '4.5', '여성', CURRENT_DATE, NOW()),
((SELECT id FROM account WHERE username = 'user10'), '010-1010-0001', '속리산', '속리산', '중급', '3.5', '남성', CURRENT_DATE, NOW());

-- -----------------------------------------
-- 3-5. 코트 데이터
--   제로제로투 피클볼 (admin1): 코트 3개
--   강남 피클볼 클럽  (admin2): 코트 2개
--   잠실 피클볼 센터  (admin3): 코트 2개
-- -----------------------------------------
INSERT INTO court (partner_id, court_name, personnel_number, court_level, court_gugun, create_date) VALUES
-- 제로제로투 피클볼
((SELECT id FROM partner WHERE business_partner = '제로제로투 피클볼'), 'A코트', 8, '초급',   '01', NOW()),
((SELECT id FROM partner WHERE business_partner = '제로제로투 피클볼'), 'B코트', 8, '중급',   '01', NOW()),
((SELECT id FROM partner WHERE business_partner = '제로제로투 피클볼'), 'C코트', 6, '상급',   '01', NOW()),
-- 강남 피클볼 클럽
((SELECT id FROM partner WHERE business_partner = '강남 피클볼 클럽'),  '1번코트', 8, '초급', '01', NOW()),
((SELECT id FROM partner WHERE business_partner = '강남 피클볼 클럽'),  '2번코트', 6, '중급', '01', NOW()),
-- 잠실 피클볼 센터
((SELECT id FROM partner WHERE business_partner = '잠실 피클볼 센터'),  '메인코트', 8, '중급', '01', NOW()),
((SELECT id FROM partner WHERE business_partner = '잠실 피클볼 센터'),  '서브코트', 6, '초급', '01', NOW());

-- -----------------------------------------
-- 3-6. 코트 스케줄 샘플 (오늘 날짜 기준)
--   제로제로투 피클볼 A코트: 4개 시간대
--   제로제로투 피클볼 B코트: 3개 시간대
-- -----------------------------------------
INSERT INTO court_schedule (court_id, game_date, start_time, end_time, schedule_type) VALUES
-- A코트 (제로제로투)
((SELECT c.id FROM court c JOIN partner p ON c.partner_id = p.id
  WHERE p.business_partner = '제로제로투 피클볼' AND c.court_name = 'A코트'),
 CURRENT_DATE, '08:00', '10:00', 'OPEN_GAME'),
((SELECT c.id FROM court c JOIN partner p ON c.partner_id = p.id
  WHERE p.business_partner = '제로제로투 피클볼' AND c.court_name = 'A코트'),
 CURRENT_DATE, '10:00', '12:00', 'OPEN_GAME'),
((SELECT c.id FROM court c JOIN partner p ON c.partner_id = p.id
  WHERE p.business_partner = '제로제로투 피클볼' AND c.court_name = 'A코트'),
 CURRENT_DATE, '14:00', '16:00', 'OPEN_GAME'),
((SELECT c.id FROM court c JOIN partner p ON c.partner_id = p.id
  WHERE p.business_partner = '제로제로투 피클볼' AND c.court_name = 'A코트'),
 CURRENT_DATE, '18:00', '20:00', 'RENTAL'),

-- B코트 (제로제로투)
((SELECT c.id FROM court c JOIN partner p ON c.partner_id = p.id
  WHERE p.business_partner = '제로제로투 피클볼' AND c.court_name = 'B코트'),
 CURRENT_DATE, '08:00', '10:00', 'OPEN_GAME'),
((SELECT c.id FROM court c JOIN partner p ON c.partner_id = p.id
  WHERE p.business_partner = '제로제로투 피클볼' AND c.court_name = 'B코트'),
 CURRENT_DATE, '10:00', '12:00', 'OPEN_GAME'),
((SELECT c.id FROM court c JOIN partner p ON c.partner_id = p.id
  WHERE p.business_partner = '제로제로투 피클볼' AND c.court_name = 'B코트'),
 CURRENT_DATE, '14:00', '16:00', 'RENTAL');


-- =============================================
-- 4. 테이블 요약
-- =============================================
--
-- | 테이블             | 설명                | PK       | 주요 FK/제약             |
-- |--------------------|---------------------|----------|--------------------------|
-- | account            | 통합 계정            | id (SEQ) | username UNIQUE          |
-- | member             | 회원 상세 정보       | id (SEQ) | account_id → account(id) |
-- | member_role        | 회원 권한            | 복합 PK  | username → account       |
-- | partner            | 사업장 정보          | id (SEQ) | account_id → account(id) |
-- | court              | 코트 정보            | id (SEQ) | partner_id → partner(id) |
-- | court_schedule     | 코트 시간대 스케줄    | id (SEQ) | court_id → court(id)     |
-- | reservation        | 예약 정보            | id (SEQ) | court_id, username       |
-- | game_result        | 게임 결과            | id (SEQ) | court_id → court(id)     |
-- | member_suspension  | 회원 정지            | id (SEQ) | username, partner_id     |
-- | reject_vote        | 거부 투표            | id (SEQ) | court_id, target, voter  |
--
-- =============================================
-- 5. 테스트 계정 요약
-- =============================================
--
-- | 유형     | username       | password    | 이름/사업장             | 역할          |
-- |----------|---------------|-------------|------------------------|---------------|
-- | 관리자   | admin          | admin1234   | 시스템관리자            | ROLE_ADMIN    |
-- | 사업장   | admin1         | admin1!123  | 제로제로투 피클볼       | ROLE_PARTNER  |
-- | 사업장   | admin2         | admin1!123  | 강남 피클볼 클럽        | ROLE_PARTNER  |
-- | 사업장   | admin3         | admin1!123  | 잠실 피클볼 센터        | ROLE_PARTNER  |
-- | 일반회원 | user1          | user1!123   | 박민수 (중급/3.5)       | ROLE_USER     |
-- | 일반회원 | user2          | user1!123   | 김영희 (초급/2.5)       | ROLE_USER     |
-- | 일반회원 | user3          | user1!123   | 이철수 (상급/4.5)       | ROLE_USER     |
-- | 테스트   | user4          | user1!123   | 백두산 (초급/2.5)       | ROLE_USER     |
-- | 테스트   | user5          | user1!123   | 한라산 (중급/3.5)       | ROLE_USER     |
-- | 테스트   | user6          | user1!123   | 지리산 (상급/4.5)       | ROLE_USER     |
-- | 테스트   | user7          | user1!123   | 설악산 (초급/2.5)       | ROLE_USER     |
-- | 테스트   | user8          | user1!123   | 북한산 (중급/3.5)       | ROLE_USER     |
-- | 테스트   | user9          | user1!123   | 금강산 (상급/4.5)       | ROLE_USER     |
-- | 테스트   | user10         | user1!123   | 속리산 (중급/3.5)       | ROLE_USER     |
--
