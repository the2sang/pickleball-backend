-- =============================================
-- V5: Court Schedule 테이블 생성
-- =============================================

CREATE TABLE court_schedule (
    id SERIAL PRIMARY KEY,
    court_id BIGINT NOT NULL,
    game_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    schedule_type VARCHAR(20) NOT NULL,
    
    CONSTRAINT fk_court_schedule_court FOREIGN KEY (court_id) REFERENCES court (id) ON DELETE CASCADE,
    CONSTRAINT uq_court_schedule_slot UNIQUE (court_id, game_date, start_time)
);
