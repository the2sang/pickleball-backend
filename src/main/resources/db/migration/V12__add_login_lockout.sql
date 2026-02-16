-- =============================================
-- Add lockout fields to login_failure
-- =============================================

ALTER TABLE login_failure
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;
