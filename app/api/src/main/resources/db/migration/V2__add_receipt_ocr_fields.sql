ALTER TABLE receipts
    ADD COLUMN image_url VARCHAR(255);

ALTER TABLE receipts
    ADD COLUMN ocr_status VARCHAR(50);

UPDATE receipts
SET image_url = '',
    ocr_status = 'PENDING';

ALTER TABLE receipts
    ALTER COLUMN image_url SET NOT NULL;

ALTER TABLE receipts
    ALTER COLUMN ocr_status SET NOT NULL;