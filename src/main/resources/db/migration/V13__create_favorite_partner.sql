CREATE TABLE favorite_partner (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    partner_id BIGINT NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_favorite_partner UNIQUE (username, partner_id),
    CONSTRAINT fk_favorite_partner_account FOREIGN KEY (username) REFERENCES account(username) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_partner_partner FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE CASCADE
);

CREATE INDEX idx_favorite_partner_username ON favorite_partner(username);
CREATE INDEX idx_favorite_partner_partner_id ON favorite_partner(partner_id);
