ALTER TABLE app.organizations
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE app.organization_memberships
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
