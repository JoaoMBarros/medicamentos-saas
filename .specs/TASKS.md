# Tasks

This file decomposes the deliveries defined in
[ROADMAP.md](./ROADMAP.md) into actionable activities.

## Delivery 1 - Technical foundation

### Project and module structure

- [x] Define the base package structure for identity, organization, catalog,
  inventory, ingestion, expiration, notification, and audit.
- [x] Define module boundaries and dependency direction.
- [x] Document local setup and required environment variables.

### Persistence

- [x] Add PostgreSQL configuration for local and production profiles.
- [x] Add Flyway and create the initial migration baseline.
- [x] Configure connection pooling and database health checks.
- [x] Add Docker Compose services for PostgreSQL and local dependencies.

### Configuration and security foundation

- [x] Separate local, test, and production YAML profiles.
- [x] Load secrets only from environment variables or a secret manager.
- [x] Add Spring Security resource-server dependencies and baseline
  authentication configuration.
- [x] Deny unauthenticated access by default.
- [x] Define the operational exception for infrastructure health probes.

### Quality and observability

- [x] Add CI workflow for build and tests.
- [x] Enforce JaCoCo minimum global line coverage of 85%.
- [x] Add structured JSON logging.
- [x] Add request correlation ID handling.
- [x] Configure Actuator liveness and readiness probes.
- [x] Add baseline Micrometer metrics.
- [x] Define the API problem-details error response.

## Delivery 2 - Identity and tenant management

### Authentication

- [x] Configure OAuth 2.0/OIDC issuer and audience validation.
- [x] Map token claims to application user and organization context.
- [x] Add authentication failure and authorization failure handlers.
- [x] Add integration tests for valid, expired, malformed, and missing tokens.

### Organizations and memberships

- [x] Create the organization domain model and migration.
- [x] Create user membership and role models.
- [x] Implement organization creation and lifecycle operations.
- [x] Implement user invitation and membership management.
- [x] Implement role and permission checks.
- [x] Add audit events for membership and permission changes.

### Tenant isolation

- [x] Add `organization_id` to every tenant-owned model and table.
- [x] Implement tenant context resolution from the authenticated identity.
- [x] Enforce tenant filtering in application services and repositories.
- [x] Add cross-tenant read and write rejection tests.
- [x] Add indexes for tenant-aware lookups.

## Delivery 3 - Medication catalog and locations

### Medication catalog

- [ ] Define the medication domain model and database migration.
- [ ] Add medication fields for name, active ingredient, concentration,
  presentation, and regulatory identifiers.
- [ ] Implement medication creation, update, and retrieval use cases.
- [ ] Prevent duplicate medications within an organization.
- [ ] Add paginated, filtered, and sorted medication search.

### Locations

- [ ] Define the location domain model and database migration.
- [ ] Implement location creation, update, and retrieval use cases.
- [ ] Support pharmacy and warehouse location types.
- [ ] Prevent invalid or cross-tenant location references.
- [ ] Add paginated location search.

### API contracts and quality

- [ ] Define immutable request and response records.
- [ ] Implement ModelMapper-based transformations outside controllers.
- [ ] Add Bean Validation to external request records.
- [ ] Add controller, service, repository, and integration tests as applicable.
- [ ] Document catalog and location endpoints.

## Delivery 4 - Manual inventory and batch management

### Stock batches

- [ ] Define the stock batch model and migration.
- [ ] Implement batch registration with lot number, expiration date, quantity,
  medication, and location.
- [ ] Validate expiration dates, quantities, status, and tenant ownership.
- [ ] Implement batch update and retrieval operations.
- [ ] Add indexes for expiration date, organization, location, and medication.

### Inventory movements

- [ ] Define the immutable inventory movement model and migration.
- [ ] Implement addition, removal, transfer, and adjustment use cases.
- [ ] Persist batch changes and movements in one transaction.
- [ ] Add optimistic locking for concurrent inventory updates.
- [ ] Prevent negative quantities unless an explicitly approved adjustment
  policy exists.

### Reliability and queries

- [ ] Implement idempotency-key storage and duplicate request handling.
- [ ] Add queries for expired and soon-to-expire batches.
- [ ] Add batch history and audit retrieval.
- [ ] Prevent destructive deletion of inventory movement history.
- [ ] Add unit, integration, concurrency, and API tests.

## Delivery 5 - File ingestion

### Ingestion lifecycle

- [ ] Define ingestion job and ingestion item models and migrations.
- [ ] Implement ingestion session creation.
- [ ] Implement job status and progress endpoints.
- [ ] Define job states: `RECEIVED`, `PROCESSING`, `PARTIAL`, `COMPLETED`, and
  `FAILED`.
- [ ] Persist accepted, rejected, duplicated, and updated item totals.

### File handling

- [ ] Configure S3-compatible object storage.
- [ ] Generate pre-signed upload URLs.
- [ ] Validate file type, size, tenant, and upload expiration.
- [ ] Implement streaming CSV and spreadsheet parsing.
- [ ] Keep parsing memory bounded for large files.

### Validation and processing

- [ ] Define the ingestion input contract and column mapping rules.
- [ ] Validate each row and persist clear row-level errors.
- [ ] Normalize medication and batch data before persistence.
- [ ] Process valid items in bounded transactions.
- [ ] Implement deterministic idempotency keys per source item.
- [ ] Ensure retries do not duplicate batches or inventory movements.

### Messaging and operations

- [ ] Configure RabbitMQ ingestion commands.
- [ ] Add retry, backoff, acknowledgement, and dead-letter handling.
- [ ] Implement worker metrics for throughput, latency, and failures.
- [ ] Add tests for partial success, retries, duplicates, malformed files, and
  worker failure recovery.

## Delivery 6 - Expiration alerts and notifications

### Expiration rules

- [ ] Define configurable expiration warning windows.
- [ ] Store non-sensitive warning configuration in YAML.
- [ ] Implement scheduled discovery of upcoming and expired batches.
- [ ] Define rules for batches already notified or no longer eligible.

### Notification lifecycle

- [ ] Define notification model and migration.
- [ ] Implement deduplication with notification idempotency keys.
- [ ] Implement tenant-level notification preferences.
- [ ] Configure the initial notification provider.
- [ ] Implement notification delivery status tracking.

### Reliability

- [ ] Configure notification commands and consumers.
- [ ] Implement retry with bounded exponential backoff.
- [ ] Add dead-letter handling for permanent failures.
- [ ] Add tests for deduplication, scheduling, delivery, retries, and failure
  isolation.

## Delivery 7 - Operational reporting and read performance

### Reports and dashboards

- [ ] Implement inventory summary queries.
- [ ] Implement expiration summary queries.
- [ ] Implement ingestion summary queries.
- [ ] Implement notification delivery summary queries.
- [ ] Add cursor-based pagination for large collections.
- [ ] Return only the columns required by each response.

### Performance

- [ ] Review and optimize tenant-aware database indexes.
- [ ] Identify slow queries using database metrics.
- [ ] Add projections for expensive aggregates when measurements justify them.
- [ ] Add Redis only for stable, high-read data with a defined invalidation
  strategy.
- [ ] Configure request size limits and API rate limiting.

### Observability and validation

- [ ] Add p50, p95, and p99 latency metrics by endpoint.
- [ ] Add database timing, connection usage, and slow-query monitoring.
- [ ] Add queue depth and worker processing-time metrics.
- [ ] Run representative load tests.
- [ ] Record baseline performance results and regression thresholds.

## Delivery 8 - Production readiness

### Infrastructure

- [ ] Provision managed PostgreSQL with backups and high availability options.
- [ ] Provision the message broker and object storage.
- [ ] Configure centralized logs, metrics, and traces.
- [ ] Configure environment-specific secret management.
- [ ] Define horizontal scaling for stateless API instances.

### Deployment and database operations

- [ ] Create the production container image.
- [ ] Add deployment pipeline with approval and rollback support.
- [ ] Add migration execution and failure handling.
- [ ] Review destructive migration procedures separately.
- [ ] Configure readiness checks before traffic is assigned.

### Security and resilience

- [ ] Add dependency and container vulnerability scanning.
- [ ] Define secret rotation procedures.
- [ ] Configure alerts for error rate, latency, queue backlog, database health,
  and storage.
- [ ] Write runbooks for ingestion failures, notification failures, and
  dependency degradation.
- [ ] Test database restoration from backup.
- [ ] Run load, resilience, and disaster-recovery tests.

## Delivery 9 - Evolution to independent services

### Extraction assessment

- [ ] Measure ingestion throughput, worker saturation, and deployment impact.
- [ ] Measure notification volume, provider failures, and retry pressure.
- [ ] Identify ownership or availability boundaries that justify extraction.
- [ ] Produce an extraction decision record with cost and risk analysis.

### Ingestion service extraction

- [ ] Define versioned ingestion commands and events.
- [ ] Define ingestion service data ownership.
- [ ] Extract the worker behind a stable contract.
- [ ] Preserve idempotency, retries, dead-letter handling, and tenant context.
- [ ] Add independent deployment and observability.

### Notification service extraction

- [ ] Define versioned notification commands and delivery events.
- [ ] Define notification service data ownership.
- [ ] Extract provider integration and retry policies.
- [ ] Preserve notification deduplication and auditability.
- [ ] Add independent deployment and operational alerts.

### Migration and validation

- [ ] Define a compatibility period for old and new consumers.
- [ ] Migrate traffic incrementally.
- [ ] Validate tenant isolation and consistency across service boundaries.
- [ ] Test replay, recovery, and rollback paths.
- [ ] Remove obsolete monolith paths only after production verification.

## Cross-cutting activities

- [ ] Keep transport-layer models immutable with Java records.
- [ ] Use ModelMapper for transformations outside controllers.
- [ ] Keep changeable, non-sensitive values in YAML configuration.
- [ ] Require authentication for every externally reachable service endpoint.
- [ ] Add tests for success, validation, boundary, and failure scenarios.
- [ ] Maintain at least 85% global line coverage with JaCoCo.
- [ ] Update architecture, stack, API, and operational documentation with each
  relevant change.
- [ ] Add only necessary Javadoc for complex or non-standard behavior.
- [ ] Review logs and error responses for secrets or unnecessary personal data.
