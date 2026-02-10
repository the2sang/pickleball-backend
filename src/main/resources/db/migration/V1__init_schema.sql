-- =============================================
-- 피클볼 예약 시스템 - PostgreSQL 초기 스키마
-- =============================================

-- 1. 사업장 (Partner)
CREATE TABLE partner (
    id              BIGSERIAL       PRIMARY KEY,
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
    update_date     TIMESTAMP
);

COMMENT ON TABLE partner IS '사업장 정보';
COMMENT ON COLUMN partner.partner_level IS '예비업체(0), 정식등록업체(1)';
COMMENT ON COLUMN partner.how_to_pay IS '예약건별 수수료(0), 월구독(1), 년구독(2)';

-- 2. 회원 (Member)
CREATE TABLE member (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(20)     NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
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
    update_date     TIMESTAMP
);

COMMENT ON TABLE member IS '회원 정보';
COMMENT ON COLUMN member.game_level IS '입문/초급/초중급/중급/중상급/상급';
COMMENT ON COLUMN member.member_level IS '정회원/게스트/정지회원';

CREATE INDEX idx_member_username ON member(username);

-- 3. 회원 권한 (Member Role)
CREATE TABLE member_role (
    username        VARCHAR(20)     NOT NULL,
    roles           VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,
    PRIMARY KEY (username, roles),
    FOREIGN KEY (username) REFERENCES member(username) ON DELETE CASCADE
);

COMMENT ON TABLE member_role IS '회원 권한';

-- 4. 코트 (Court)
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
    FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE CASCADE
);

COMMENT ON TABLE court IS '코트 정보';
COMMENT ON COLUMN court.court_level IS '초급/중급/상급';
COMMENT ON COLUMN court.reserv_close IS '예약마감 여부 (Y/N)';
COMMENT ON COLUMN court.court_gugun IS '코트 성격 구분';

CREATE INDEX idx_court_partner ON court(partner_id);
CREATE INDEX idx_court_date ON court(game_date);

-- 5. 예약 (Reservation)
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
    FOREIGN KEY (court_id) REFERENCES court(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES member(username)
);

COMMENT ON TABLE reservation IS '예약 정보';
COMMENT ON COLUMN reservation.reserv_type IS '일반예약(0), 특별예약(1)';

CREATE INDEX idx_reservation_court_date ON reservation(court_id, game_date, time_slot);
CREATE INDEX idx_reservation_username ON reservation(username);

-- 6. 게임결과 (Game Result)
CREATE TABLE game_result (
    id              BIGSERIAL       PRIMARY KEY,
    court_id        BIGINT          NOT NULL,
    game_result     VARCHAR(20),
    game_end        VARCHAR(2)      DEFAULT 'N',
    game_date       DATE,
    time_slot       VARCHAR(20),
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP,
    FOREIGN KEY (court_id) REFERENCES court(id) ON DELETE CASCADE
);

COMMENT ON TABLE game_result IS '게임 결과';

-- 7. 회원 정지 (Member Suspension)
CREATE TABLE member_suspension (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(20)     NOT NULL,
    partner_id      BIGINT          NOT NULL,
    suspend_type    VARCHAR(10)     NOT NULL,
    suspend_start   DATE            NOT NULL,
    suspend_end     DATE            NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    FOREIGN KEY (username) REFERENCES member(username),
    FOREIGN KEY (partner_id) REFERENCES partner(id)
);

COMMENT ON TABLE member_suspension IS '회원 정지 정보 (사업장별)';
COMMENT ON COLUMN member_suspension.suspend_type IS '1주/2주/3주/1개월/2개월/3개월/영구';

CREATE INDEX idx_suspension_lookup ON member_suspension(username, partner_id, is_active);

-- 8. 거부 투표 (Reject Vote)
CREATE TABLE reject_vote (
    id              BIGSERIAL       PRIMARY KEY,
    court_id        BIGINT          NOT NULL,
    time_slot       VARCHAR(20)     NOT NULL,
    game_date       DATE            NOT NULL,
    target_user     VARCHAR(20)     NOT NULL,
    voter_user      VARCHAR(20)     NOT NULL,
    is_reject       BOOLEAN         NOT NULL DEFAULT TRUE,
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    FOREIGN KEY (court_id) REFERENCES court(id),
    FOREIGN KEY (target_user) REFERENCES member(username),
    FOREIGN KEY (voter_user) REFERENCES member(username),
    UNIQUE (court_id, time_slot, game_date, target_user, voter_user)
);

COMMENT ON TABLE reject_vote IS '게임 참가 거부 투표';
