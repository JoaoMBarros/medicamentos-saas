# Roadmap

## Direction

Build a secure, multi-tenant SaaS backend for medication expiration
management. Deliver a reliable modular monolith first, validate the core
workflow with real users, and extract asynchronous workloads only when
measured scale or operational needs justify it.

## Delivery 1 - Technical foundation

### Objective

Create a reproducible and secure foundation for all future development.

### Outcomes

- Java 21 and Spring Boot project configured with Gradle.
- Modular package structure for identity, organization, catalog, inventory,
  ingestion, expiration, notification, and audit.
- PostgreSQL connection and Flyway migration baseline.
- Environment-specific YAML configuration for non-sensitive values.
- Secret handling through environment variables or a secret manager.
- Docker-based local development dependencies.
- CI pipeline with build, tests, JaCoCo, and the 85% minimum line coverage.
- Structured logs, correlation IDs, Actuator health, and basic metrics.
- API error format based on problem details.

### Exit criteria

- A new developer can run the application and its dependencies locally.
- `gradle check` passes with the configured coverage threshold.
- No protected endpoint is available without authentication configuration.

## Delivery 2 - Identity and tenant management

### Objective

Establish secure multi-tenancy and organization access control.

### Outcomes

- OAuth 2.0/OIDC resource server integration.
- Organization creation and lifecycle.
- User invitation and membership management.
- Roles and permissions for operators and managers.
- Organization context resolved from the authenticated identity.
- Tenant filtering enforced in application services and repositories.
- Audit events for membership and permission changes.

### Exit criteria

- An authenticated user can access only authorized organizations.
- Cross-tenant reads and writes are rejected and covered by integration tests.
- Authentication failures and authorization failures return safe responses.

## Delivery 3 - Medication catalog and locations

### Objective

Provide normalized medication and stock-location data for inventory operations.

### Outcomes

- Medication catalog CRUD with duplicate prevention.
- Support for active ingredient, concentration, presentation, and identifiers.
- Location CRUD for pharmacies and warehouses.
- Immutable transport records for requests and responses.
- ModelMapper-based transformations outside controllers.
- Pagination, filtering, and sorting on collection endpoints.

### Exit criteria

- Users can create and search medications and locations within their tenant.
- Validation errors are explicit and documented.
- The main catalog queries meet the initial latency targets.

## Delivery 4 - Manual inventory and batch management

### Objective

Enable the core workflow: registering medication batches and monitoring expiry.

### Outcomes

- Stock batch registration with lot number, expiration date, quantity, and
  location.
- Inventory movements for additions, removals, transfers, and adjustments.
- Transactional consistency between batch changes and movements.
- Optimistic locking for concurrent inventory updates.
- Idempotency keys for retriable writes.
- Queries for expired and soon-to-expire batches.
- Audit trail without destructive deletion of inventory history.

### Exit criteria

- A user can register a batch and immediately query its status.
- Invalid dates, quantities, locations, and duplicate requests are rejected.
- Concurrent updates cannot silently lose inventory changes.
- Core use cases have unit and integration test coverage.

## Delivery 5 - File ingestion

### Objective

Allow users and integrations to import large medication and batch datasets
without blocking API requests.

### Outcomes

- Ingestion job creation and status endpoints.
- Pre-signed object-storage upload for CSV or spreadsheet files.
- Streaming parser with bounded memory usage.
- Row-level validation and normalized error details.
- Partial success support for valid and invalid rows.
- Idempotent item processing and safe retries.
- Job states: `RECEIVED`, `PROCESSING`, `PARTIAL`, `COMPLETED`, and `FAILED`.
- Ingestion summary with accepted, rejected, duplicated, and updated items.
- RabbitMQ command queue and dead-letter handling.

### Exit criteria

- Large files are processed asynchronously without request timeouts.
- Retrying a job does not duplicate inventory or movements.
- Users can identify and correct rejected rows.
- Worker failures are observable and recoverable.

## Delivery 6 - Expiration alerts and notifications

### Objective

Proactively notify users about medication batches that need action.

### Outcomes

- Configurable expiration warning windows.
- Scheduled discovery of upcoming and already expired batches.
- Deduplicated notification records.
- Notification delivery worker with retry and backoff.
- Initial notification channel integration.
- Delivery status and operational failure visibility.
- Tenant-level notification preferences.

### Exit criteria

- Each eligible batch produces at most one notification per configured window.
- Delivery failures are retried and eventually isolated in a dead-letter flow.
- Users can distinguish pending, delivered, and failed notifications.

## Delivery 7 - Operational reporting and read performance

### Objective

Make the platform useful for daily operations at increasing tenant and
inventory volumes.

### Outcomes

- Dashboard queries for inventory, expiration, ingestion, and notifications.
- Cursor-based pagination for large collections.
- Query tuning and tenant-aware indexes.
- Read projections for expensive aggregates when measurements justify them.
- Redis caching only for stable, high-read data.
- Performance dashboards for p95/p99 latency, slow queries, queue depth, and
  worker processing time.
- Rate limiting and request size limits for public APIs.

### Exit criteria

- Initial read and write latency targets are measured in a representative
  environment.
- No dashboard endpoint loads an unbounded dataset into application memory.
- Performance regressions are detectable in CI or staging.

## Delivery 8 - Production readiness

### Objective

Operate the SaaS reliably, securely, and with controlled tenant growth.

### Outcomes

- Managed PostgreSQL, message broker, object storage, and observability.
- Automated backup and tested database restoration.
- Deployment pipeline with safe rollback.
- Database migration review and monitoring.
- Security hardening, dependency scanning, and secret rotation.
- Alerts for error rate, latency, queue backlog, database health, and storage.
- Runbooks for ingestion failures, notification failures, and degraded
  dependencies.
- Load, resilience, and disaster-recovery tests.

### Exit criteria

- A production incident has a documented detection, mitigation, and recovery
  path.
- Backups have been restored successfully in a controlled test.
- Security and operational checks have owners and alert thresholds.

## Delivery 9 - Evolution to independent services

### Objective

Extract services only when evidence shows that the modular monolith no longer
meets scaling, ownership, or availability needs.

### Candidates

- Ingestion service for independent worker scaling and file-processing load.
- Notification service for provider isolation and retry policies.
- Identity service only if organization and product boundaries require it.

### Outcomes

- Explicit service contracts and versioned events.
- Independent deployment and observability.
- Data ownership boundaries and migration strategy.
- Idempotent consumers and replay-safe event handling.
- Operational cost and failure-mode review before each extraction.

### Exit criteria

- Extraction is supported by measured bottlenecks or clear ownership needs.
- The extracted service can operate and recover independently.
- No migration reduces tenant isolation, auditability, or consistency guarantees.

## Cross-cutting quality gates

Every delivery must:

- preserve tenant isolation and authentication requirements;
- use immutable transport records;
- use ModelMapper for transformations;
- keep non-sensitive changeable values in YAML configuration;
- maintain at least 85% global line coverage enforced by JaCoCo;
- include tests for success, validation, boundary, and failure scenarios;
- update relevant architecture, stack, API, or operational documentation;
- expose errors explicitly and avoid silent fallbacks;
- avoid secrets, unrelated refactoring, and premature infrastructure.

## Prioritization rule

Prioritize work that reduces risk in this order:

1. tenant isolation and authentication;
2. transactional correctness and auditability;
3. data quality and idempotent ingestion;
4. observability and recoverability;
5. latency and scale optimization;
6. service extraction and infrastructure specialization.
