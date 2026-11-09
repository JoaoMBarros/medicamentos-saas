# Stack

## 1. Recommended baseline

| Area | Technology | Reason |
|---|---|---|
| Language | Java 21 | LTS release, strong typing, modern language features, and long support window |
| Application framework | Spring Boot 3.5.x | Mature HTTP, configuration, validation, observability, and security ecosystem |
| Build | Gradle | Fast incremental builds, dependency management, and good multi-module evolution |
| API | Spring MVC | Predictable request handling, broad library compatibility, and sufficient performance for CRUD and ingestion commands |
| Transport models | Java records | Immutable request and response contracts |
| Validation | Jakarta Bean Validation | Declarative validation at the API boundary |
| Mapping | ModelMapper | Centralized transformation between records, domain objects, and persistence models |
| Security | Spring Security + OAuth 2.0 Resource Server | JWT validation and endpoint protection without implementing identity management |
| Database | PostgreSQL | ACID transactions, indexes, constraints, JSONB when needed, and reliable operational tooling |
| Persistence | Spring Data JPA/Hibernate | Productive relational mapping and transaction support |
| Migrations | Flyway | Versioned, reviewable, repeatable database evolution |
| Cache | Redis, when justified by metrics | Low-latency cache and short-lived coordination data; not the source of truth for inventory |
| Messaging | RabbitMQ initially | Straightforward work queues, retries, acknowledgements, and dead-letter exchanges |
| Object storage | S3-compatible storage | Durable storage for ingestion files and export artifacts |
| Observability | Spring Boot Actuator + Micrometer + OpenTelemetry | Health, metrics, traces, and correlation across API and workers |
| Logs | Structured JSON logs | Searchable operational diagnostics without parsing free-form text |
| Tests | JUnit 5, Spring Boot Test, MockMvc, Testcontainers | Unit, integration, HTTP, and real dependency verification |
| Coverage | JaCoCo | Enforced minimum of 85% line coverage through Gradle `check` |
| Packaging | Container image | Reproducible deployment and simple horizontal scaling |

## 2. API and request performance

Spring MVC is the initial API choice because the workload is mainly
transactional CRUD, validation, and paginated reads. It avoids introducing
reactive complexity before there is evidence that the application is
I/O-bound at a scale where WebFlux provides a material benefit.

Use:

- JSON over HTTPS;
- records for immutable request and response models;
- explicit DTOs instead of exposing JPA entities;
- pagination, filtering, and sorting on collection endpoints;
- `Idempotency-Key` for retriable writes;
- correlation IDs in requests and responses;
- RFC 7807-style problem details for errors;
- compression only for payloads large enough to benefit from it.

For large ingestion files, use a pre-signed object-storage upload instead of
sending the complete file through the API process.

## 3. Database design

PostgreSQL is the system of record because stock updates, inventory movements,
tenant isolation, expiration queries, and audit history require relational
constraints and transactions.

Recommended database practices:

- UUID or time-sortable identifiers for public identifiers;
- UTC timestamps with explicit timezone handling;
- database constraints for quantities, statuses, and required relationships;
- composite tenant-aware indexes;
- optimistic locking for concurrent stock updates;
- separate read projections only when measured query load requires them;
- no hard delete for auditable inventory movements.

Use JSONB only for source-specific ingestion metadata or evolving integration
payloads, not for core fields that require relational constraints and indexes.

## 4. Asynchronous processing

RabbitMQ is recommended for the first asynchronous workloads:

- ingestion commands;
- ingestion result events;
- expiration notification commands;
- retry and dead-letter flows.

If event volume, replay requirements, or analytics justify it later, evaluate
Kafka or a managed streaming platform. Do not introduce Kafka solely for CRUD
events in the initial version.

## 5. Authentication and authorization

Use an external identity provider compatible with OpenID Connect and OAuth 2.0.
The backend acts as a resource server:

1. the client authenticates with the identity provider;
2. the client sends a bearer access token;
3. Spring Security validates token signature, issuer, audience, and expiry;
4. application authorization checks organization membership and permissions.

Every endpoint is protected by default. Explicitly document and narrowly
configure any operational endpoint that must be reachable by infrastructure.

## 6. Configuration

Keep non-sensitive values that may change without a code deployment in
`application.yml` and environment-specific profiles such as
`application-local.yml` or `application-prod.yml`.

Examples:

- expiration warning windows;
- pagination defaults and maximums;
- queue names;
- feature flags;
- notification schedules;
- external service URLs.

Inject secrets through a secret manager or environment variables. Never commit
credentials, private keys, or tokens in YAML.

## 7. Deployment shape

Initial deployment:

- one Spring Boot application image;
- one PostgreSQL instance or managed database;
- one Redis instance only if caching is enabled;
- one RabbitMQ instance or managed broker;
- one object-storage bucket;
- separate worker process or profile for asynchronous consumers when required;
- centralized metrics, logs, traces, and alerting.

Scale the API horizontally behind a load balancer. Keep the application
stateless; store sessions, jobs, locks, and durable state in managed services.

## 8. Decision criteria for later changes

Reconsider a technology only with evidence from:

- p95/p99 latency and throughput;
- database CPU, locks, slow queries, and connection usage;
- queue depth and processing time;
- error rate and retry volume;
- deployment and operational complexity;
- cost per tenant and expected growth.

The stack should evolve from measured bottlenecks, not from premature
distribution or technology preference.
