CREATE TABLE categories (
    id UUID PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(20),
    color VARCHAR(20),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT chk_categories_type
        CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_categories_system_user
        CHECK ((is_system = TRUE AND user_id IS NULL) OR (is_system = FALSE AND user_id IS NOT NULL))
);

CREATE INDEX idx_categories_user_active ON categories(user_id, is_active);
CREATE INDEX idx_categories_type_active ON categories(type, is_active);
CREATE INDEX idx_categories_system_active ON categories(is_system, is_active);
