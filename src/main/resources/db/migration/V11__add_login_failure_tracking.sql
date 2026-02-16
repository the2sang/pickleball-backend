-- =============================================
-- Login failure tracking (for email notifications)
-- =============================================

CREATE TABLE IF NOT EXISTS login_failure (
    username          VARCHAR(20) PRIMARY KEY,
    fail_count        INTEGER      NOT NULL DEFAULT 0,
    last_failed_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_notified_at  TIMESTAMP,
    create_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
    update_date       TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_login_failure_last_failed_at
    ON login_failure(last_failed_at);
