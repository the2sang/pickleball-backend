CREATE TABLE IF NOT EXISTS circle (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT UNIQUE,
    business_partner VARCHAR(100) NOT NULL,
    owner VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    partner_address VARCHAR(200) NOT NULL,
    partner_level VARCHAR(10) NOT NULL DEFAULT '0',
    partner_email VARCHAR(100) NOT NULL,
    regist_date DATE NOT NULL,
    agree_all_yn VARCHAR(1) NOT NULL DEFAULT 'N',
    agree_service_yn VARCHAR(1) NOT NULL DEFAULT 'N',
    agree_privacy_yn VARCHAR(1) NOT NULL DEFAULT 'N',
    agree_marketing_yn VARCHAR(1) NOT NULL DEFAULT 'N',
    partner_account VARCHAR(50),
    partner_bank VARCHAR(100),
    how_to_pay VARCHAR(10),
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    update_date TIMESTAMP,
    CONSTRAINT fk_circle_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
);

COMMENT ON TABLE circle IS '동호회 정보';
COMMENT ON COLUMN circle.partner_level IS '예비동호회(0), 활성동호회(1)';
COMMENT ON COLUMN circle.how_to_pay IS '예약건별 수수료(0), 월구독(1), 년구독(2)';

CREATE INDEX IF NOT EXISTS idx_circle_business_partner ON circle(business_partner);
CREATE INDEX IF NOT EXISTS idx_circle_partner_level ON circle(partner_level);
