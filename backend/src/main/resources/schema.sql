CREATE TABLE IF NOT EXISTS transfers (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_reference      VARCHAR(100)   NOT NULL,
    source_account_id      VARCHAR(50)    NOT NULL,
    destination_account_id VARCHAR(50)    NOT NULL,
    amount                 DECIMAL(19, 2) NOT NULL,
    status                 VARCHAR(20)    NOT NULL,
    reason                 VARCHAR(255),
    processed_at           TIMESTAMP(9)   NOT NULL,
    CONSTRAINT uq_transfers_request_reference UNIQUE (request_reference)
);

CREATE INDEX IF NOT EXISTS idx_transfers_source_account_processed_at
    ON transfers (source_account_id, processed_at);
