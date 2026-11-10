# Delivery 2 Implementation Report

This report summarizes the implemented product capabilities at a high level.
It is not a substitute for the architecture, roadmap, or task specification.

## 2026-11-10T03:49:01Z - Lombok policy alignment

**Delivered behavior:**

- Added the Lombok compile-only and annotation-processor setup, including the
  required SpotBugs annotations dependency and root `lombok.config`.
- Replaced constructor boilerplate in the organization service and controller
  with `@RequiredArgsConstructor`.
- Replaced entity getter and protected no-argument-constructor boilerplate with
  `@Getter` and `@NoArgsConstructor(access = PROTECTED)`.
- Added ID-only `@ToString` generation for entities so generated text does not
  expose usernames, roles, audit details, or other sensitive state.
- Kept DTOs as records, entity equality/hash code handwritten, and domain
  constructors and state-changing methods explicit.

**Validation:** `.\gradlew.bat test --tests
br.com.medicacao.saas.api.* --tests br.com.medicacao.saas.config.* --tests
br.com.medicacao.saas.identity.* --tests br.com.medicacao.saas.organization.*`
passed. `.\gradlew.bat check` compiled the project and ran 18 tests, but one
Spring integration test failed during Testcontainers initialization because the
Java process could not find a valid Docker environment; no test assertion
failed.

**Assumptions / limitations:** No endpoint or database schema changed. Lombok
generated code is excluded from coverage through `lombok.addLombokGeneratedAnnotation`;
behavioral entity methods remain covered by existing tests.

**Endpoints (curl):** Not applicable; this is a structure-only refactor.

## 2026-11-10T03:41:22Z - SDD practice alignment

**Delivered behavior:**

- Replaced scattered OIDC `@Value` injection with validated,
  configuration-properties binding.
- Standardized invalid request responses on HTTP 422 Problem Details and
  included correlation IDs in application and security error responses.
- Added immutable authenticated-user invariants, tenant-scoped membership
  lookup, optimistic locking for organization and membership updates, and
  UTC clock injection for audit events.
- Added bounded membership pagination (default 20, maximum 100) and an
  immutable page response instead of returning an unbounded collection.
- Replaced the H2 integration-test substitute with PostgreSQL Testcontainers.
- Added the `version` field to `organizations` and
  `organization_memberships`; Hibernate uses it as the optimistic-lock
  counter so concurrent lifecycle or role changes fail instead of silently
  overwriting one another.

**Validation:** Java 21 compilation passed. Targeted unit tests for API,
configuration, identity, and organization modules passed. The full
`.\gradlew.bat check` and the PostgreSQL integration test remain blocked by
Testcontainers' inability to find a valid Docker environment in this session.
Acceptance criteria: AC-1 configuration binding, AC-2 protected tenant
operations, AC-3 explicit validation/error contract, AC-4 concurrent update
protection, and AC-5 PostgreSQL-backed deterministic integration setup.

**Assumptions / limitations:** No new endpoint was introduced; the existing
membership collection now accepts `page` and `size` query parameters. The full
Gradle check remains pending in an environment where Testcontainers can
connect to Docker.

**Endpoints (curl):** No new endpoints. The changed collection contract is:

```bash
curl -i -X GET \
  "http://localhost:8080/api/organizations/<organization-id>/memberships?page=0&size=20" \
  -H "Authorization: ******" \
  -H "Accept: application/json"
```

Existing protected examples are documented below.

**Database:** Migration `V3__add_optimistic_lock_versions.sql` adds the
non-null `version` counter (default `0`) to `organizations` and
`organization_memberships`. Existing fields and tenant/uniqueness/index
constraints remain unchanged.

## 2026-11-10 00:08 (-03:00) - GitHub Actions validation for feature branches

- Configured GitHub Actions to run on every push to branches beginning with
  `feature` or `fix`.
- Configured pull request validation when targeting `develop` or `master`.
- Separated tests, quality and coverage verification, and application build
  into visible workflow steps.
- Added an explicit GitHub Actions step that fails when JaCoCo line coverage
  falls below the required 85% minimum.
- Any failed step marks the GitHub Actions check as failed and reports the
  commit as unsuccessful.

## 2026-11-09 19:25 (-03:00) - Local Keycloak infrastructure

- Added a persistent Keycloak service to the local Docker Compose environment.
- Exposed the local identity provider on port 8081 with configurable bootstrap
  administrator credentials.
- Updated the example OIDC configuration to use the `medicacao` realm and its
  standard issuer and JWKS endpoints.
- The realm, clients, users, roles, and protocol mappers still require
  configuration in the Keycloak administration console.

Relevant local token request:

```bash
curl -X POST \
  "http://localhost:8081/realms/medicacao/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=medicacao-cli" \
  -d "client_secret=<client-secret>" \
  -d "username=<username>" \
  -d "password=<password>"
```

Password Grant is documented here only for local development and testing.
Client applications should use Authorization Code with PKCE.

## 2026-11-09 18:58 (-03:00) - Organizations and tenant isolation

- Implemented the organization domain and its lifecycle management.
- Added organization memberships with roles and membership status management.
- Added organization creation, invitations, role changes, removal, suspension,
  and archival operations.
- Added audit records for organization and membership changes.
- Added tenant context resolution from the authenticated identity.
- Enforced organization boundary checks for protected organization operations.
- Added persistence migration, constraints, and tenant-aware indexes.
- Added tests covering organization rules, permissions, and cross-tenant
  access rejection.

Relevant API requests:

```bash
curl -X POST "http://localhost:8080/api/organizations" \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Organization 1","slug":"organization-1"}'

curl "http://localhost:8080/api/organizations/<organization-id>/memberships" \
  -H "Authorization: Bearer <access-token>"

curl -X POST \
  "http://localhost:8080/api/organizations/<organization-id>/memberships" \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-123","username":"operator","role":"OPERATOR"}'

curl -X PATCH \
  "http://localhost:8080/api/organizations/<organization-id>/memberships/<membership-id>/role" \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"role":"ADMIN"}'

curl -X DELETE \
  "http://localhost:8080/api/organizations/<organization-id>/memberships/<membership-id>" \
  -H "Authorization: Bearer <access-token>"
```

### Persistence model

The migration `V2__create_organizations_and_memberships.sql` creates:

- `organizations`: stores each tenant's identity (`id`), human-readable
  `name`, unique URL-safe `slug`, lifecycle `status`, and audit timestamps
  (`created_at` and `updated_at`).
- `organization_memberships`: links an external identity (`user_id` and
  `username`) to an organization (`organization_id`), stores its application
  `role` and membership `status`, and records creation/update timestamps.
  The organization/user uniqueness constraint prevents duplicate memberships.
- `organization_audit_events`: records the organization (`organization_id`),
  acting user (`actor_user_id`), event category (`event_type`), optional target
  user (`target_user_id`), contextual details, and event timestamp
  (`created_at`).

Foreign keys preserve organization ownership boundaries. The membership and
audit indexes support tenant-scoped lookups and chronological audit retrieval.

## 2026-11-09 17:45 (-03:00) - Secure JWT authentication

- Implemented OAuth 2.0/OIDC resource-server authentication with JWT.
- Added issuer, audience, signature, and expiration validation.
- Mapped authenticated identity, organization, and roles into the application
  security context.
- Added standard unauthorized and forbidden responses.
- Added automated tests for valid and invalid authentication scenarios.

Relevant API request:

```bash
curl "http://localhost:8080/actuator/metrics" \
  -H "Authorization: Bearer <access-token>"
```

## Previous foundation - Delivery 1

- Established the Java 21, Spring Boot, and Gradle project foundation.
- Added modular package boundaries for the main product domains.
- Added PostgreSQL, Flyway, Hikari, Docker Compose, and environment-based
  configuration.
- Added structured logging, correlation IDs, health probes, metrics, problem
  details, CI validation, and JaCoCo coverage enforcement.

Relevant health requests:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```

## Reporting convention

Every newly implemented feature must add a concise, high-level entry to this
report with the completion date and time. The entry must summarize delivered
behavior, relevant validation, and notable limitations without duplicating
implementation details from the source code.

Each delivery entry must also include the pertinent Bash `curl` examples for
its externally observable endpoints or flows. When a delivery creates database
tables, its entry must explain the high-level purpose of each table and field,
including constraints and indexes when they affect the behavior.
