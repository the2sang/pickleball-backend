ALTER TABLE member
    ADD COLUMN IF NOT EXISTS agree_all_yn VARCHAR(1);

ALTER TABLE member
    ADD COLUMN IF NOT EXISTS agree_service_yn VARCHAR(1);

ALTER TABLE member
    ADD COLUMN IF NOT EXISTS agree_privacy_yn VARCHAR(1);

ALTER TABLE member
    ADD COLUMN IF NOT EXISTS agree_marketing_yn VARCHAR(1);

UPDATE member
SET agree_all_yn = 'N'
WHERE agree_all_yn IS NULL;

UPDATE member
SET agree_service_yn = 'N'
WHERE agree_service_yn IS NULL;

UPDATE member
SET agree_privacy_yn = 'N'
WHERE agree_privacy_yn IS NULL;

UPDATE member
SET agree_marketing_yn = 'N'
WHERE agree_marketing_yn IS NULL;

ALTER TABLE member
    ALTER COLUMN agree_all_yn SET DEFAULT 'N';

ALTER TABLE member
    ALTER COLUMN agree_all_yn SET NOT NULL;

ALTER TABLE member
    ALTER COLUMN agree_service_yn SET DEFAULT 'N';

ALTER TABLE member
    ALTER COLUMN agree_service_yn SET NOT NULL;

ALTER TABLE member
    ALTER COLUMN agree_privacy_yn SET DEFAULT 'N';

ALTER TABLE member
    ALTER COLUMN agree_privacy_yn SET NOT NULL;

ALTER TABLE member
    ALTER COLUMN agree_marketing_yn SET DEFAULT 'N';

ALTER TABLE member
    ALTER COLUMN agree_marketing_yn SET NOT NULL;
