CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(40),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE wallet (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES app_user(id),
    balance NUMERIC(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT wallet_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE wallet_transaction (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    source_wallet_id UUID REFERENCES wallet(id),
    destination_wallet_id UUID REFERENCES wallet(id),
    amount NUMERIC(19,2) NOT NULL,
    balance_after NUMERIC(19,2),
    trace_id VARCHAR(100) NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT transaction_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_wallet_tx_source_created ON wallet_transaction(source_wallet_id, created_at DESC);
CREATE INDEX idx_wallet_tx_destination_created ON wallet_transaction(destination_wallet_id, created_at DESC);
CREATE INDEX idx_wallet_tx_trace_id ON wallet_transaction(trace_id);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    trace_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_unpublished ON outbox_event(created_at) WHERE published_at IS NULL;
