CREATE TABLE monitors (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    target_url VARCHAR(500) NOT NULL,
    interval_seconds INT NOT NULL,
    timeout_ms INT NOT NULL DEFAULT 5000,
    last_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    consecutive_failures INT NOT NULL DEFAULT 0,
    next_check_at TIMESTAMPTZ NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_monitors_due_checks
    ON monitors (next_check_at)
    WHERE active = TRUE;

CREATE TABLE check_executions (
    id UUID PRIMARY KEY,
    monitor_id UUID NOT NULL REFERENCES monitors(id) ON DELETE CASCADE,
    http_status INT,
    latency_ms INT NOT NULL,
    success BOOLEAN NOT NULL,
    error_message VARCHAR(500),
    checked_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_check_executions_monitor_time
    ON check_executions (monitor_id, checked_at DESC);

CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY,
    monitor_id UUID NOT NULL REFERENCES monitors(id) ON DELETE CASCADE,
    callback_url VARCHAR(500) NOT NULL,
    secret_key VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE webhook_subscription_events (
    subscription_id UUID NOT NULL REFERENCES webhook_subscriptions(id) ON DELETE CASCADE,
    event_type VARCHAR(30) NOT NULL,
    PRIMARY KEY (subscription_id, event_type)
);

CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES webhook_subscriptions(id) ON DELETE CASCADE,
    event_type VARCHAR(30) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    last_http_status INT,
    last_error VARCHAR(500),
    next_retry_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_deliveries_subscription
    ON webhook_deliveries (subscription_id, created_at DESC);