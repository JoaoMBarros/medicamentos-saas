CREATE TABLE app.locations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES app.organizations (id),
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_location_type CHECK (type IN ('PHARMACY', 'WAREHOUSE')),
    CONSTRAINT uk_location_organization_name_type UNIQUE (organization_id, name, type)
);

CREATE INDEX idx_locations_organization_name
    ON app.locations (organization_id, name);
