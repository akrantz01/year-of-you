CREATE TABLE captures
(
    id         BIGINT ${autoincrement} PRIMARY KEY,
    account_id BIGINT      NOT NULL,
    front      VARCHAR(32) NOT NULL,
    back       VARCHAR(32) NOT NULL,
    swapped    BOOLEAN     NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    CONSTRAINT fk_captures_account_id__id FOREIGN KEY (account_id)
        REFERENCES accounts (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    CONSTRAINT chk_captures_unsigned_integer_id CHECK (id >= 0)
);
