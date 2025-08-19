CREATE TABLE orders (
    id UUID PRIMARY KEY,
    status TEXT NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE order_item (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders(id),
    product_sku TEXT NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    qty INT NOT NULL
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
