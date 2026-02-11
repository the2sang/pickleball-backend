-- =============================================
-- V2: Account 테이블 분리 - 통합 인증
-- =============================================

-- 1. account 테이블 생성
CREATE TABLE account (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(20)     NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    account_type    VARCHAR(10)     NOT NULL DEFAULT 'MEMBER',
    name            VARCHAR(50)     NOT NULL,
    create_date     TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMP
);

COMMENT ON TABLE account IS '통합 계정 정보';
COMMENT ON COLUMN account.account_type IS 'MEMBER(일반회원), PARTNER(사업장)';

CREATE INDEX idx_account_username ON account(username);
CREATE INDEX idx_account_type ON account(account_type);

-- 2. 기존 member 데이터를 account로 이관
INSERT INTO account (username, password, account_type, name, create_date, update_date)
SELECT username, password, 'MEMBER', name, create_date, update_date
FROM member;

-- 3. member 테이블에 account_id 컬럼 추가
ALTER TABLE member ADD COLUMN account_id BIGINT;

-- 기존 데이터에 account_id 매핑
UPDATE member m
SET account_id = a.id
FROM account a
WHERE a.username = m.username;

-- account_id NOT NULL 제약 조건 추가
ALTER TABLE member ALTER COLUMN account_id SET NOT NULL;

-- account_id FK 추가
ALTER TABLE member ADD CONSTRAINT fk_member_account
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE;

-- account_id UNIQUE 제약 추가 (1:1 관계)
ALTER TABLE member ADD CONSTRAINT uq_member_account_id UNIQUE (account_id);

-- 4. member_role FK를 account.username으로 변경
ALTER TABLE member_role DROP CONSTRAINT IF EXISTS member_role_username_fkey;
ALTER TABLE member_role ADD CONSTRAINT fk_member_role_account
    FOREIGN KEY (username) REFERENCES account(username) ON DELETE CASCADE;

-- 5. reservation FK를 account.username으로 변경
ALTER TABLE reservation DROP CONSTRAINT IF EXISTS reservation_username_fkey;
ALTER TABLE reservation ADD CONSTRAINT fk_reservation_account
    FOREIGN KEY (username) REFERENCES account(username);

-- 6. member_suspension FK를 account.username으로 변경
ALTER TABLE member_suspension DROP CONSTRAINT IF EXISTS member_suspension_username_fkey;
ALTER TABLE member_suspension ADD CONSTRAINT fk_member_suspension_account
    FOREIGN KEY (username) REFERENCES account(username);

-- 7. reject_vote FK를 account.username으로 변경
ALTER TABLE reject_vote DROP CONSTRAINT IF EXISTS reject_vote_target_user_fkey;
ALTER TABLE reject_vote DROP CONSTRAINT IF EXISTS reject_vote_voter_user_fkey;
ALTER TABLE reject_vote ADD CONSTRAINT fk_reject_vote_target_account
    FOREIGN KEY (target_user) REFERENCES account(username);
ALTER TABLE reject_vote ADD CONSTRAINT fk_reject_vote_voter_account
    FOREIGN KEY (voter_user) REFERENCES account(username);

-- 8. member 테이블에서 username, password 컬럼 제거
ALTER TABLE member DROP CONSTRAINT IF EXISTS member_username_key;
DROP INDEX IF EXISTS idx_member_username;
ALTER TABLE member DROP COLUMN username;
ALTER TABLE member DROP COLUMN password;

-- 9. partner 테이블에 account_id 컬럼 추가
ALTER TABLE partner ADD COLUMN account_id BIGINT;
ALTER TABLE partner ADD CONSTRAINT fk_partner_account
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE;
ALTER TABLE partner ADD CONSTRAINT uq_partner_account_id UNIQUE (account_id);
