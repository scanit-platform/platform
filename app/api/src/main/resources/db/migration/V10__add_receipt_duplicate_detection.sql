ALTER TABLE receipts
    ADD COLUMN image_hash VARCHAR(64);

ALTER TABLE receipts
    ADD COLUMN is_duplicate BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE receipts
    ADD COLUMN saved_as_duplicate BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE receipts
    ADD COLUMN duplicate_match_reason VARCHAR(30);

ALTER TABLE receipts
    ADD COLUMN duplicate_of_receipt_id BIGINT;

ALTER TABLE receipts
    ADD CONSTRAINT fk_receipts_duplicate_of
        FOREIGN KEY (duplicate_of_receipt_id)
            REFERENCES receipts(id)
            ON DELETE SET NULL;

CREATE INDEX idx_receipts_user_image_hash
    ON receipts(user_id, image_hash);

CREATE INDEX idx_receipts_duplicate_details
    ON receipts(user_id, vendor_name, total_amount, transaction_date);

CREATE INDEX idx_receipts_duplicate_of
    ON receipts(duplicate_of_receipt_id);