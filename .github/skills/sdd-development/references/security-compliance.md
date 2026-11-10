# Security and compliance

## Authentication and authorization
- Every microservice is protected by authentication; no endpoint is exposed
  without the required auth configuration **and tests** proving it.
- Bean-based `SecurityFilterChain`; never `WebSecurityConfigurerAdapter`.
- Stateless, JWT/OAuth2 Resource Server; authorize by role or scope, with
  `@PreAuthorize` where appropriate.
- External OIDC provider (e.g. Keycloak) is the Authorization Server. Client
  apps use Authorization Code + PKCE; this app stays a Resource Server.
- Password Grant only for local development or automated tests, never
  production. If used, add a `CONCERNS.md` item (`Review pre prod deploy`).

## Data isolation and privacy
- Isolate tenant data in every query; explicitly test cross-tenant leakage.
- Never log personal or health data (names, national IDs, diagnoses,
  medications). Log opaque identifiers.
- Health-data systems in Brazil: LGPD applies. Specs touching personal or
  health data must state the legal basis, retention period, and who may access
  it; if the spec is silent, ask or record it as an open concern rather than
  deciding silently.
- Encrypt in transit (TLS) and at rest where applicable.
- Keep an audit trail (who changed what, when) for sensitive domain data.

## Hardening
- Validate and sanitize all external input; restrictive CORS.
- Keep dependencies current; run OWASP Dependency-Check or Dependabot when
  configured.
- Disable or protect sensitive Actuator endpoints.
- No secrets, credentials, or personal data in code, tests fixtures, or Git.
