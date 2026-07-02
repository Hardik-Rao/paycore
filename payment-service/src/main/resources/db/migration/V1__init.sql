CREATE TABLE accounts (
    id              UUID PRIMARY KEY,
    vpa             VARCHAR(255) NOT NULL UNIQUE,
    account_holder  VARCHAR(255) NOT NULL,
    account_type    VARCHAR(32)  NOT NULL,
    kyc_status      VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
    id               UUID PRIMARY KEY,
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    payer_vpa        VARCHAR(255) NOT NULL,
    payee_vpa        VARCHAR(255) NOT NULL,
    amount           NUMERIC(19, 4) NOT NULL,
    currency         VARCHAR(3) NOT NULL DEFAULT 'INR',
    status           VARCHAR(32) NOT NULL,
    fraud_score      INTEGER,
    failure_reason   TEXT,
    reversal_reason  TEXT,
    metadata         JSONB,
    initiated_at     TIMESTAMPTZ NOT NULL,
    processed_at     TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payments_payer ON payments(payer_vpa);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_initiated_at ON payments(initiated_at);

CREATE TABLE payment_audit_logs (
    id          UUID PRIMARY KEY,
    payment_id  UUID NOT NULL REFERENCES payments(id),
    old_status  VARCHAR(32),
    new_status  VARCHAR(32) NOT NULL,
    changed_by  VARCHAR(255) NOT NULL,
    changed_at  TIMESTAMPTZ NOT NULL,
    reason      TEXT
);

CREATE INDEX idx_audit_payment ON payment_audit_logs(payment_id);
