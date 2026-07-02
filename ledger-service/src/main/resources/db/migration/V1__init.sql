CREATE TABLE account_balances (
    account_id    UUID PRIMARY KEY,
    vpa           VARCHAR(255) NOT NULL UNIQUE,
    balance       NUMERIC(19, 4) NOT NULL DEFAULT 0,
    version       BIGINT NOT NULL DEFAULT 0,
    last_updated  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE ledger_entries (
    id             UUID PRIMARY KEY,
    payment_id     UUID NOT NULL,
    account_id     UUID NOT NULL REFERENCES account_balances(account_id),
    entry_type     VARCHAR(16) NOT NULL,
    amount         NUMERIC(19, 4) NOT NULL,
    currency       VARCHAR(3) NOT NULL DEFAULT 'INR',
    balance_after  NUMERIC(19, 4) NOT NULL,
    description    TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_payment ON ledger_entries(payment_id);
CREATE INDEX idx_ledger_account_created ON ledger_entries(account_id, created_at);

CREATE TABLE daily_ledger_summaries (
    id               UUID PRIMARY KEY,
    account_id       UUID NOT NULL REFERENCES account_balances(account_id),
    summary_date     DATE NOT NULL,
    opening_balance  NUMERIC(19, 4) NOT NULL,
    closing_balance  NUMERIC(19, 4) NOT NULL,
    total_debits     NUMERIC(19, 4) NOT NULL DEFAULT 0,
    total_credits    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    entry_count      INTEGER NOT NULL DEFAULT 0,
    UNIQUE (account_id, summary_date)
);
