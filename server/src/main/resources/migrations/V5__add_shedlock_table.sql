CREATE TABLE shedlock
(
    name       VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP(6) NOT NULL,
    locked_at  TIMESTAMP(6) NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);
