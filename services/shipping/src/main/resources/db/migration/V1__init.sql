CREATE TABLE shipment (
    id UUID PRIMARY KEY,
    order_id UUID UNIQUE NOT NULL,
    status TEXT NOT NULL,
    carrier TEXT NOT NULL,
    tracking TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregate_type TEXT NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,
    event_key TEXT NOT NULL UNIQUE
);
