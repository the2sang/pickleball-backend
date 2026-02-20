ALTER TABLE court
ALTER COLUMN personnel_number SET DEFAULT 30;

UPDATE court
SET personnel_number = 30
WHERE personnel_number IS NULL;
