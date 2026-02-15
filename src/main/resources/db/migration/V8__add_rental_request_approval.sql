ALTER TABLE reservation
    ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20);

UPDATE reservation
SET approval_status = 'APPROVED'
WHERE approval_status IS NULL;

ALTER TABLE reservation
    ALTER COLUMN approval_status SET DEFAULT 'APPROVED';

ALTER TABLE reservation
    ALTER COLUMN approval_status SET NOT NULL;

ALTER TABLE court_schedule
    ADD COLUMN IF NOT EXISTS locked_yn VARCHAR(1);

UPDATE court_schedule
SET locked_yn = 'N'
WHERE locked_yn IS NULL;

ALTER TABLE court_schedule
    ALTER COLUMN locked_yn SET DEFAULT 'N';

ALTER TABLE court_schedule
    ALTER COLUMN locked_yn SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_reservation_approval
    ON reservation (court_id, game_date, time_slot, approval_status);
