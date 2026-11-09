# Architecture

## 1. Context

The system is a backend-only SaaS for managing medication expiration dates.
It must support multiple organizations, manual registration by users, bulk
ingestion from external sources, expiration monitoring, traceability, and
notifications.

The first version should optimize for reliability, low operational complexity,
and fast reads. It should start as a modular monolith with clear domain
boundaries. Modules can be extracted into microservices only when scale or
team boundaries justify the operational cost.

## 2. Main actors

- **Organization**: tenant that owns pharmacies, warehouses, locations, users,
  medications, batches, and inventory.
- **User**: authenticated operator or manager who registers and monitors stock.
- **Ingestion source**: CSV, spreadsheet, API, or integration that sends
  medication and batch data.
- **Notification worker**: asynchronous process that sends alerts for upcoming
  expiration and ingestion results.

## 3. Domain model

The minimum domain should contain:

- `Organization`: tenant and subscription boundary.
- `User` and `Role`: identity and permissions.
- `Location`: pharmacy, warehouse, or stock location.
- `Medication`: normalized catalog data such as name, active ingredient,
  concentration, presentation, and regulatory identifiers.
- `StockBatch`: a medication lot with expiration date, quantity, location,
  supplier, and status.
- `InventoryMovement`: immutable audit of additions, removals, transfers, and
  adjustments.
- `IngestionJob`: source, status, progress, timestamps, and error summary.
- `IngestionItem`: raw or normalized item, validation status, and error details.
- `Notification`: recipient, type, delivery status, and idempotency key.

Every tenant-owned table must include `organization_id`. Tenant filtering must
be enforced in the application service layer and covered by integration tests.

## 4. Core flows

### 4.1 User registration and access

1. A user is invited or created within an organization.
2. The identity provider authenticates the user and issues an access token.
3. The API validates the token and resolves organization, user, and roles.
4. Authorization checks permissions before every protected operation.
5. Authentication and authorization events are logged without exposing tokens
   or sensitive personal data.

### 4.2 Manual medication and batch registration

1. The client sends a validated request containing medication, lot, quantity,
   location, and expiration date.
2. The API validates required fields, date rules, quantity, and tenant context.
3. The application service finds or creates the normalized medication.
4. The service creates or updates the stock batch using an idempotency key.
5. An inventory movement is recorded in the same database transaction.
6. The API returns the immutable response record with the resource identifier.
7. An outbox event may trigger asynchronous notifications or projections.

Manual operations should remain synchronous because the user needs immediate
validation and a deterministic result.

### 4.3 Bulk ingestion

1. The client requests an ingestion session and receives an `ingestion_job_id`.
2. The client uploads a CSV, spreadsheet, or payload to object storage.
3. The API records metadata and publishes an ingestion command.
4. A worker streams the file, validates each item, normalizes medication data,
   and processes valid batches in bounded transactions.
5. Invalid items are persisted with row-level error details; one invalid row
   must not silently invalidate unrelated valid rows.
6. Reprocessing uses a deterministic idempotency key based on source, tenant,
   external identifier, and item version.
7. The job status changes through `RECEIVED`, `PROCESSING`, `PARTIAL`,
   `COMPLETED`, or `FAILED`.
8. The client polls job status or receives a webhook/event when processing ends.
9. A summary with accepted, rejected, duplicated, and updated items is
   available for audit.

Ingestion must be asynchronous because it can be slow, retried, and larger than
the request timeout budget.

### 4.4 Expiration monitoring

1. A scheduled job finds batches approaching configured expiration windows.
2. It creates deduplicated notification records using an idempotency key.
3. A worker delivers notifications through configured channels.
4. Failures are retried with backoff and eventually moved to a dead-letter
   state for operational review.
5. Expired, blocked, or consumed batches remain traceable and are never
   physically deleted as part of normal operation.

### 4.5 Queries and dashboards

The read path should support:

- batches expiring in a configurable period;
- already expired batches;
- inventory by location and medication;
- ingestion progress and rejected rows;
- audit history for a batch;
- notification delivery status.

Use indexed, paginated queries and projections for dashboard aggregates. Avoid
loading complete inventories into application memory.

## 5. Proposed boundaries

Start with modules inside one deployable application:

- `identity`: authentication integration and authorization.
- `organization`: tenant and membership management.
- `catalog`: medication normalization.
- `inventory`: batches, quantities, locations, and movements.
- `ingestion`: uploads, parsing, validation, and job state.
- `expiration`: rules for expiration windows and status.
- `notification`: outbox consumption and delivery.
- `audit`: immutable operational history.

Each module should expose application use cases instead of allowing controllers
to call repositories directly.

## 6. Consistency and messaging

- Use database transactions for a batch change and its inventory movement.
- Use an outbox table to publish reliable domain events after a transaction.
- Use idempotency keys for manual writes, ingestion items, and notifications.
- Prefer at-least-once delivery with idempotent consumers over an assumption of
  exactly-once processing.
- Keep synchronous requests short; delegate parsing, notifications, and
  projections to workers.

## 7. Performance strategy

- Use cursor-based pagination for large batch and audit listings.
- Add indexes for `(organization_id, expiration_date)`,
  `(organization_id, location_id, medication_id)`, and ingestion status fields.
- Select only the columns required by each response.
- Use connection pooling with bounded limits.
- Cache stable catalog data and configuration only after measuring the access
  pattern; never use cache as the source of truth for stock.
- Add request correlation IDs, latency metrics, database timings, and queue
  depth metrics.
- Define initial targets such as p95 under 300 ms for ordinary reads and
  p95 under 700 ms for synchronous writes, excluding external provider latency.

## 8. Security and tenancy

- All microservices and all externally reachable endpoints require
  authentication.
- Authorization must verify organization membership and required role or
  permission on the server.
- Use an external OpenID Connect/OAuth 2.0 identity provider rather than
  implementing password storage in the application initially.
- Store secrets in a secret manager or environment configuration, never in YAML
  committed to the repository.
- Encrypt traffic in transit and protect stored backups.
- Audit changes to inventory, permissions, ingestion, and notifications.

## 9. Reliability and operations

- Health checks must distinguish liveness from readiness.
- Use structured logs with correlation and tenant-safe identifiers.
- Add retries only to transient failures and cap them with backoff.
- Use dead-letter handling for messages that cannot be processed.
- Back up the transactional database and periodically test restoration.
- Apply schema migrations forward-only and review destructive changes separately.

## 10. Evolution path

Do not split services at the start. Extract a module only when there is a
measured reason, such as independent scaling for ingestion, notification
isolation, team ownership, or a separate availability requirement.

The first likely extraction candidates are `ingestion` and `notification`,
because both are naturally asynchronous and have different workload profiles.
