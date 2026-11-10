CREATE TABLE app.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE app.organization_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES app.organizations (id),
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_membership_organization_user UNIQUE (organization_id, user_id)
);

CREATE TABLE app.organization_audit_events (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES app.organizations (id),
    actor_user_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    target_user_id VARCHAR(255),
    details VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_membership_organization ON app.organization_memberships (organization_id);
CREATE INDEX idx_audit_organization_created ON app.organization_audit_events (organization_id, created_at);
