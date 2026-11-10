CREATE TABLE app.stock_batches (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES app.organizations (id),
    medication_id UUID NOT NULL REFERENCES app.medications (id),
    location_id UUID NOT NULL REFERENCES app.locations (id),
    lot_number VARCHAR(100) NOT NULL,
    expiration_date DATE NOT NULL,
    quantity NUMERIC(19, 3) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_batch_quantity_positive CHECK (quantity >= 0),
    CONSTRAINT uk_batch_organization_medication_lot
        UNIQUE (organization_id, medication_id, lot_number)
);

CREATE INDEX idx_batches_organization_expiration
    ON app.stock_batches (organization_id, expiration_date);
CREATE INDEX idx_batches_organization_medication
    ON app.stock_batches (organization_id, medication_id);
CREATE INDEX idx_batches_organization_location
    ON app.stock_batches (organization_id, location_id);

CREATE TABLE app.inventory_movements (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES app.organizations (id),
    batch_id UUID NOT NULL REFERENCES app.stock_batches (id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('ADDITION', 'REMOVAL', 'TRANSFER', 'ADJUSTMENT')),
    quantity NUMERIC(19, 3) NOT NULL,
    from_location_id UUID REFERENCES app.locations (id),
    to_location_id UUID REFERENCES app.locations (id),
    idempotency_key VARCHAR(200) NOT NULL,
    actor_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_movement_idempotency UNIQUE (organization_id, type, idempotency_key)
);

CREATE INDEX idx_movements_organization_batch_created
    ON app.inventory_movements (organization_id, batch_id, created_at);

CREATE TABLE app.idempotency_records (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES app.organizations (id),
    operation VARCHAR(80) NOT NULL,
    idempotency_key VARCHAR(200) NOT NULL,
    resource_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_idempotency_organization_operation_key
        UNIQUE (organization_id, operation, idempotency_key)
);
