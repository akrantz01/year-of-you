CREATE TABLE accounts (
    id BIGINT ${autoincrement} PRIMARY KEY,
    display_name VARCHAR(64) NOT NULL,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(512) NOT NULL,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_accounts_unsigned_integer_id CHECK (id >= 0)
);
