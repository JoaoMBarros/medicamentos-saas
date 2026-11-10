# Spring Boot architecture, REST and configuration

## Structure
- Package-by-feature: controller, service, repository, DTOs and domain types
  together.
- Dependency direction: Controller (HTTP) -> Service (rules, transactions) ->
  Repository (persistence). Controllers never touch repositories or hold
  business rules.
- Constructor injection with `final` fields; no field `@Autowired`; a single
  constructor needs no `@Autowired`. Never mix `@Autowired` with manual `new` of
  Spring beans.
- Interfaces only with more than one real implementation or a meaningful
  boundary.

## Mapping (ModelMapper)
- Transport models are immutable `record`s. Never expose JPA entities.
- Use ModelMapper-based mappers between transport, domain and persistence
  models; keep mapping out of controllers.
- Records and ModelMapper: use a single shared, configured `ModelMapper` bean
  (strict matching) on a version that supports records (3.2+; confirm the
  project version). Every mapper gets a test asserting field-by-field
  correctness, and `modelMapper.validate()` runs in a test. If a record with
  compact-constructor invariants cannot be mapped reliably, use an explicit
  constructor call or a `Converter` in the mapper class. Do not add setters to
  records to satisfy the mapper.

## Configuration
- `@ConfigurationProperties` with records and `@Validated`; avoid scattered
  `@Value`.
- `application-{profile}.yml` profiles. Secrets in environment variables or a
  secret manager, never in the repository.

## REST
- Correct verbs and statuses: `201` + `Location` on creation, `204` with no
  body, `404`, `409`, `422` where they apply.
- `@Valid`/`@Validated` with Jakarta Bean Validation on external DTOs.
- Global errors via `@RestControllerAdvice` returning `ProblemDetail`
  (RFC 9457) with a correlation/trace ID. Never expose stack traces or internal
  messages.
- Collection endpoints use `Pageable`: default page size 20, hard maximum 100
  (configurable in YAML); never return unbounded lists.
- New public APIs go under `/api/v1/...` unless an existing endpoint requires
  another path.
- Non-idempotent creates that clients may retry (payments, prescriptions,
  orders) accept an `Idempotency-Key` header, or the spec states why not.
- Document public endpoints with springdoc-openapi when available.
- Every new endpoint gets a working Bash `curl` example (see
  `references/templates.md`).

## HTTP clients and exceptions
- `RestClient` (or `WebClient` for reactive flows); never `RestTemplate`.
  Always configure connect and read timeouts.
- Specific unchecked domain exceptions with clear messages. Do not catch
  generic `Exception`, swallow failures, or use exceptions for ordinary control
  flow.
- Log an error once at the boundary (normally the global advice), not in every
  layer.
