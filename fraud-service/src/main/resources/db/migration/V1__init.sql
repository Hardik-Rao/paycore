CREATE TABLE fraud_rules (
    id              UUID PRIMARY KEY,
    rule_name       VARCHAR(128) NOT NULL,
    rule_type       VARCHAR(64) NOT NULL,
    threshold       NUMERIC(19, 4),
    window_seconds  INTEGER,
    action          VARCHAR(32) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE fraud_alerts (
    id               UUID PRIMARY KEY,
    payment_id       UUID NOT NULL,
    rule_id          UUID,
    fraud_score      INTEGER NOT NULL,
    triggered_rules  JSONB NOT NULL DEFAULT '[]',
    action_taken     VARCHAR(32) NOT NULL,
    reviewed_by      VARCHAR(255),
    reviewed_at      TIMESTAMPTZ,
    resolution       VARCHAR(32),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE device_fingerprints (
    id             UUID PRIMARY KEY,
    account_id     VARCHAR(255) NOT NULL,
    device_hash    VARCHAR(255) NOT NULL,
    first_seen     TIMESTAMPTZ NOT NULL,
    last_seen      TIMESTAMPTZ NOT NULL,
    payment_count  INTEGER NOT NULL DEFAULT 0,
    is_trusted     BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (account_id, device_hash)
);

CREATE TABLE payment_evaluations (
    payment_id  UUID PRIMARY KEY,
    fraud_score INTEGER NOT NULL,
    action      VARCHAR(32) NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL
);

INSERT INTO fraud_rules (id, rule_name, rule_type, threshold, window_seconds, action, is_active) VALUES
('11111111-1111-1111-1111-111111111101', 'Velocity Check', 'VELOCITY', 5, 60, 'FLAG', true),
('11111111-1111-1111-1111-111111111102', 'Amount Threshold', 'AMOUNT_THRESHOLD', 100000, NULL, 'FLAG', true),
('11111111-1111-1111-1111-111111111103', 'New Device', 'NEW_DEVICE', 10000, NULL, 'FLAG', true),
('11111111-1111-1111-1111-111111111104', 'Night Large', 'NIGHT_LARGE', 50000, NULL, 'FLAG', true),
('11111111-1111-1111-1111-111111111105', 'Round Amount', 'ROUND_AMOUNT', NULL, NULL, 'FLAG', true),
('11111111-1111-1111-1111-111111111106', 'Inactive Account', 'INACTIVE_ACCOUNT', 30, NULL, 'FLAG', true);
