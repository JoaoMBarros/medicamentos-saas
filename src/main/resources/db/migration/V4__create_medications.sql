CREATE TABLE app.medications (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES app.organizations (id),
    name VARCHAR(120) NOT NULL,
    active_ingredient VARCHAR(160) NOT NULL,
    concentration VARCHAR(80) NOT NULL,
    presentation VARCHAR(120) NOT NULL,
    anvisa_registration VARCHAR(40),
    ean VARCHAR(14),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_medication_catalog_identity UNIQUE (
        organization_id, name, active_ingredient, concentration, presentation
    )
);

CREATE INDEX idx_medications_organization_name
    ON app.medications (organization_id, name);

CREATE INDEX idx_medications_organization_active_ingredient
    ON app.medications (organization_id, active_ingredient);
