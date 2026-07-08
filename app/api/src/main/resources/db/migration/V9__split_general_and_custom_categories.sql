CREATE TABLE general_categories (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    sort_order INTEGER NOT NULL DEFAULT 0
);

INSERT INTO general_categories (id, code, name, icon, color, sort_order)
SELECT
    id,
    CASE
        WHEN LOWER(name) = 'groceries' THEN 'GROCERIES'
        WHEN LOWER(name) = 'food & dining' THEN 'FOOD_DINING'
        WHEN LOWER(name) = 'shopping' THEN 'SHOPPING'
        WHEN LOWER(name) = 'transport' THEN 'TRANSPORT'
        WHEN LOWER(name) = 'utilities' THEN 'UTILITIES'
        WHEN LOWER(name) = 'healthcare' THEN 'HEALTHCARE'
        WHEN LOWER(name) = 'entertainment' THEN 'ENTERTAINMENT'
        WHEN LOWER(name) = 'other' THEN 'OTHER'
        WHEN LOWER(name) = 'salary' THEN 'SALARY'
        WHEN LOWER(name) = 'bonus' THEN 'BONUS'
        WHEN LOWER(name) = 'investment' THEN 'INVESTMENT'
        WHEN LOWER(name) = 'other income' THEN 'OTHER_INCOME'
        ELSE CONCAT('LEGACY_', REPLACE(CAST(id AS VARCHAR), '-', ''))
    END,
    name,
    icon,
    color,
    CASE
        WHEN LOWER(name) = 'food & dining' THEN 10
        WHEN LOWER(name) = 'groceries' THEN 20
        WHEN LOWER(name) = 'shopping' THEN 30
        WHEN LOWER(name) = 'transport' THEN 40
        WHEN LOWER(name) = 'utilities' THEN 50
        WHEN LOWER(name) = 'healthcare' THEN 60
        WHEN LOWER(name) = 'entertainment' THEN 80
        WHEN LOWER(name) = 'other' THEN 900
        ELSE 1000
    END
FROM categories
WHERE user_id IS NULL;

INSERT INTO general_categories (id, code, name, icon, color, sort_order)
SELECT '00000000-0000-0000-0000-000000000013', 'TRAVEL', 'Travel', NULL, NULL, 70
WHERE NOT EXISTS (
    SELECT 1
    FROM general_categories
    WHERE code = 'TRAVEL'
);

CREATE TABLE custom_categories (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    general_category_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_custom_categories_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_custom_categories_general_category
        FOREIGN KEY (general_category_id)
        REFERENCES general_categories(id),
    CONSTRAINT uk_custom_categories_user_general_name
        UNIQUE (user_id, general_category_id, normalized_name)
);

INSERT INTO custom_categories (id, user_id, general_category_id, name, normalized_name, created_at, updated_at)
SELECT
    id,
    user_id,
    (SELECT id FROM general_categories WHERE code = 'OTHER'),
    name,
    LOWER(TRIM(name)),
    created_at,
    updated_at
FROM categories
WHERE user_id IS NOT NULL;

CREATE INDEX idx_custom_categories_user_general
    ON custom_categories(user_id, general_category_id);

ALTER TABLE budget_limit ADD COLUMN general_category_uuid UUID;
ALTER TABLE budget_limit ADD COLUMN custom_category_id UUID;

UPDATE budget_limit
SET custom_category_id = category_id
WHERE EXISTS (
    SELECT 1
    FROM custom_categories cc
    WHERE cc.id = budget_limit.category_id
);

UPDATE budget_limit
SET general_category_uuid = (
    SELECT cc.general_category_id
    FROM custom_categories cc
    WHERE cc.id = budget_limit.category_id
)
WHERE custom_category_id IS NOT NULL;

UPDATE budget_limit
SET general_category_uuid = category_id
WHERE general_category_uuid IS NULL;

ALTER TABLE budget_limit ALTER COLUMN general_category_uuid SET NOT NULL;
ALTER TABLE budget_limit DROP CONSTRAINT fk_budget_limit_category;
ALTER TABLE budget_limit DROP COLUMN category_id;
ALTER TABLE budget_limit RENAME COLUMN general_category_uuid TO general_category_id;

ALTER TABLE budget_limit
    ADD CONSTRAINT fk_budget_limit_general_category
    FOREIGN KEY (general_category_id)
    REFERENCES general_categories(id);

ALTER TABLE budget_limit
    ADD CONSTRAINT fk_budget_limit_custom_category
    FOREIGN KEY (custom_category_id)
    REFERENCES custom_categories(id);

ALTER TABLE budget_limit
    ADD CONSTRAINT chk_budget_limit_custom_requires_general
    CHECK (custom_category_id IS NULL OR general_category_id IS NOT NULL);

CREATE INDEX idx_budget_limit_general_category ON budget_limit(general_category_id);
CREATE INDEX idx_budget_limit_custom_category ON budget_limit(custom_category_id);

ALTER TABLE receipt_line_item ADD COLUMN general_category_uuid UUID;
ALTER TABLE receipt_line_item ADD COLUMN custom_category_id UUID;

UPDATE receipt_line_item
SET custom_category_id = category_id
WHERE category_id IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM custom_categories cc
      WHERE cc.id = receipt_line_item.category_id
  );

UPDATE receipt_line_item
SET general_category_uuid = (
    SELECT cc.general_category_id
    FROM custom_categories cc
    WHERE cc.id = receipt_line_item.category_id
)
WHERE custom_category_id IS NOT NULL;

UPDATE receipt_line_item
SET general_category_uuid = category_id
WHERE category_id IS NOT NULL
  AND general_category_uuid IS NULL;

ALTER TABLE receipt_line_item DROP CONSTRAINT fk_line_item_category;
ALTER TABLE receipt_line_item DROP COLUMN category_id;
ALTER TABLE receipt_line_item RENAME COLUMN general_category_uuid TO general_category_id;

ALTER TABLE receipt_line_item
    ADD CONSTRAINT fk_line_item_general_category
    FOREIGN KEY (general_category_id)
    REFERENCES general_categories(id);

ALTER TABLE receipt_line_item
    ADD CONSTRAINT fk_line_item_custom_category
    FOREIGN KEY (custom_category_id)
    REFERENCES custom_categories(id);

ALTER TABLE receipt_line_item
    ADD CONSTRAINT chk_line_item_custom_requires_general
    CHECK (custom_category_id IS NULL OR general_category_id IS NOT NULL);

CREATE INDEX idx_receipt_line_item_general_category ON receipt_line_item(general_category_id);
CREATE INDEX idx_receipt_line_item_custom_category ON receipt_line_item(custom_category_id);

ALTER TABLE receipts ADD COLUMN general_category_id UUID;
ALTER TABLE receipts ADD COLUMN custom_category_id UUID;

UPDATE receipts
SET general_category_id = (
    SELECT li.general_category_id
    FROM receipt_line_item li
    WHERE li.receipt_id = receipts.id
      AND li.general_category_id IS NOT NULL
    ORDER BY li.id
    LIMIT 1
);

UPDATE receipts
SET custom_category_id = (
    SELECT li.custom_category_id
    FROM receipt_line_item li
    WHERE li.receipt_id = receipts.id
      AND li.custom_category_id IS NOT NULL
    ORDER BY li.id
    LIMIT 1
);

ALTER TABLE receipts
    ADD CONSTRAINT fk_receipts_general_category
    FOREIGN KEY (general_category_id)
    REFERENCES general_categories(id);

ALTER TABLE receipts
    ADD CONSTRAINT fk_receipts_custom_category
    FOREIGN KEY (custom_category_id)
    REFERENCES custom_categories(id);

ALTER TABLE receipts
    ADD CONSTRAINT chk_receipts_custom_requires_general
    CHECK (custom_category_id IS NULL OR general_category_id IS NOT NULL);

CREATE INDEX idx_receipts_general_category ON receipts(general_category_id);
CREATE INDEX idx_receipts_custom_category ON receipts(custom_category_id);

DROP TABLE categories;
