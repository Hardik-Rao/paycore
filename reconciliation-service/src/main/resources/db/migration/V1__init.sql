CREATE TABLE reconciliation_reports (
    id                   UUID PRIMARY KEY,
    report_date          DATE NOT NULL UNIQUE,
    total_payments       BIGINT NOT NULL DEFAULT 0,
    total_volume         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    success_count        BIGINT NOT NULL DEFAULT 0,
    failed_count         BIGINT NOT NULL DEFAULT 0,
    reversed_count       BIGINT NOT NULL DEFAULT 0,
    fraud_blocked_count  BIGINT NOT NULL DEFAULT 0,
    mismatch_count       BIGINT NOT NULL DEFAULT 0,
    status               VARCHAR(32) NOT NULL,
    generated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE reconciliation_mismatches (
    id                UUID PRIMARY KEY,
    report_id         UUID NOT NULL REFERENCES reconciliation_reports(id),
    payment_id        UUID,
    mismatch_type     VARCHAR(64) NOT NULL,
    expected_value    TEXT,
    actual_value      TEXT,
    resolved_at       TIMESTAMPTZ,
    resolution_notes  TEXT
);

CREATE TABLE disputes (
    id                UUID PRIMARY KEY,
    payment_id        UUID NOT NULL,
    raised_by         VARCHAR(255) NOT NULL,
    reason            TEXT NOT NULL,
    status            VARCHAR(32) NOT NULL,
    evidence          JSONB,
    raised_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at       TIMESTAMPTZ,
    resolved_by       VARCHAR(255),
    resolution_notes  TEXT,
    refund_amount     NUMERIC(19, 4)
);
