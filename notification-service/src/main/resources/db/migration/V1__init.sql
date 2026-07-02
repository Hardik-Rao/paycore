CREATE TABLE webhook_subscriptions (
    id            UUID PRIMARY KEY,
    merchant_id   VARCHAR(255) NOT NULL,
    endpoint_url  TEXT NOT NULL,
    secret_key    VARCHAR(255) NOT NULL,
    events        JSONB NOT NULL DEFAULT '[]',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE webhook_deliveries (
    id                    UUID PRIMARY KEY,
    subscription_id       UUID NOT NULL REFERENCES webhook_subscriptions(id),
    payment_id            UUID,
    event_type            VARCHAR(64) NOT NULL,
    payload               JSONB NOT NULL,
    status                VARCHAR(32) NOT NULL,
    attempts              INTEGER NOT NULL DEFAULT 0,
    last_attempt_at       TIMESTAMPTZ,
    next_retry_at         TIMESTAMPTZ,
    response_status_code  INTEGER,
    response_body         TEXT
);

CREATE TABLE notification_logs (
    id            UUID PRIMARY KEY,
    payment_id    UUID,
    channel       VARCHAR(32) NOT NULL,
    recipient     VARCHAR(255) NOT NULL,
    status        VARCHAR(32) NOT NULL,
    sent_at       TIMESTAMPTZ NOT NULL,
    error_message TEXT
);
