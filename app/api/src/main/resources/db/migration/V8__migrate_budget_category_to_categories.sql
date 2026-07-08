INSERT INTO categories (id, user_id, name, type, icon, color, is_system, is_active, created_at, updated_at)
SELECT
    CAST(CONCAT('10000000-0000-0000-0000-', LPAD(CAST(bc.id AS VARCHAR), 12, '0')) AS UUID),
    NULL,
    LEFT(bc.category, 100),
    'EXPENSE',
    NULL,
    NULL,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM budget_category bc
WHERE NOT EXISTS (
    SELECT 1
    FROM categories c
    WHERE c.user_id IS NULL
      AND c.type = 'EXPENSE'
      AND LOWER(c.name) = LOWER(LEFT(bc.category, 100))
);

ALTER TABLE budget_limit ADD COLUMN category_uuid UUID;

UPDATE budget_limit
SET category_uuid = (
    SELECT c.id
    FROM budget_category bc
    JOIN categories c
      ON c.user_id IS NULL
     AND c.type = 'EXPENSE'
     AND LOWER(c.name) = LOWER(LEFT(bc.category, 100))
    WHERE bc.id = budget_limit.category_id
);

ALTER TABLE budget_limit ALTER COLUMN category_uuid SET NOT NULL;

ALTER TABLE receipt_line_item ADD COLUMN category_uuid UUID;

UPDATE receipt_line_item
SET category_uuid = (
    SELECT c.id
    FROM budget_category bc
    JOIN categories c
      ON c.user_id IS NULL
     AND c.type = 'EXPENSE'
     AND LOWER(c.name) = LOWER(LEFT(bc.category, 100))
    WHERE bc.id = receipt_line_item.category_id
)
WHERE category_id IS NOT NULL;

ALTER TABLE budget_limit DROP CONSTRAINT fk_budget_limit_category;
ALTER TABLE receipt_line_item DROP CONSTRAINT fk_line_item_category;

ALTER TABLE budget_limit DROP COLUMN category_id;
ALTER TABLE receipt_line_item DROP COLUMN category_id;

ALTER TABLE budget_limit RENAME COLUMN category_uuid TO category_id;
ALTER TABLE receipt_line_item RENAME COLUMN category_uuid TO category_id;

ALTER TABLE budget_limit
    ADD CONSTRAINT fk_budget_limit_category
    FOREIGN KEY (category_id)
    REFERENCES categories(id);

ALTER TABLE receipt_line_item
    ADD CONSTRAINT fk_line_item_category
    FOREIGN KEY (category_id)
    REFERENCES categories(id);

CREATE INDEX idx_budget_limit_category ON budget_limit(category_id);
CREATE INDEX idx_receipt_line_item_category ON receipt_line_item(category_id);

DROP TABLE budget_category;
