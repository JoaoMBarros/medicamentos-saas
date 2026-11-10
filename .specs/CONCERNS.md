# Deployment Concerns

This file tracks decisions that are acceptable for local development or
testing but must be reviewed before deployment. Items should be resolved,
validated, or explicitly accepted by the responsible team before the
corresponding deployment.

## Review pre development deploy

- [ ] Local integration tests now require Docker/Testcontainers for PostgreSQL;
  verify the shared development CI and developer environments provide a
  supported Docker runtime before relying on `./gradlew check`.

Review these items before deploying to a shared development environment.

- [ ] Replace the local Keycloak `start-dev` mode with a deployment-appropriate
  configuration and persistence strategy.
- [ ] Replace default or example Keycloak administrator credentials with
  environment-managed secrets.
- [ ] Confirm the Keycloak realm, clients, roles, protocol mappers, issuer,
  JWKS endpoint, and `medicacao-api` audience for the development environment.
- [ ] Replace the local-only Password Grant testing flow with Authorization Code
  with PKCE for client applications.
- [ ] Confirm that development clients do not expose client secrets in frontend
  code and that redirect URIs are restricted to approved origins.
- [ ] Review the use of plain HTTP localhost URLs and configure HTTPS or the
  approved development TLS strategy.
- [ ] Confirm that organization membership data and Keycloak user attributes
  are provisioned consistently for development tenants.

## Review pre prod deploy

Review these items before deploying to production.

- [ ] Disable Password Grant and verify that all production clients use
  Authorization Code with PKCE or another explicitly approved OAuth2 flow.
- [ ] Replace all example credentials, local defaults, and bootstrap secrets
  with managed production secrets and rotation procedures.
- [ ] Run Keycloak in a production-supported mode with externalized database,
  backups, high availability, hostname configuration, and key rotation.
- [ ] Validate production issuer, JWKS endpoint, audience, TLS certificates,
  CORS, redirect URIs, and token lifetimes.
- [ ] Confirm the production strategy for user lifecycle, MFA, account
  recovery, session revocation, and administrative access.
- [ ] Review tenant isolation, organization membership synchronization, audit
  retention, and cross-tenant access tests against production configuration.
- [ ] Confirm database migration execution, backups, restore procedures, and
  monitoring for organization, membership, and audit tables.
- [ ] Review observability, rate limiting, request limits, security headers,
  incident response, and secret exposure controls.

## Maintenance rule

Whenever a decision is made only to support local development or testing, add a
checkbox to the appropriate section before considering the activity complete.
Close the checkbox only after the decision has been reviewed and replaced,
validated, or explicitly accepted for that deployment environment.
