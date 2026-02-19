CREATE TABLE IF NOT EXISTS circle_place (
    id BIGSERIAL PRIMARY KEY,
    circle_id BIGINT NOT NULL,
    place_name VARCHAR(100) NOT NULL,
    personnel_number SMALLINT NOT NULL DEFAULT 30,
    place_type VARCHAR(20),
    game_time VARCHAR(20),
    reserv_close VARCHAR(1) NOT NULL DEFAULT 'N',
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    update_date TIMESTAMP,
    CONSTRAINT fk_circle_place_circle FOREIGN KEY (circle_id) REFERENCES circle(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_circle_place_circle_id ON circle_place(circle_id);

COMMENT ON TABLE circle_place IS '동호회 운동장소 정보';
COMMENT ON COLUMN circle_place.reserv_close IS '참가신청 가능 여부 (N: 가능, Y: 마감)';
