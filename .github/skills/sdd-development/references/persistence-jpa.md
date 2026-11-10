# Persistence: Spring Data JPA and Hibernate

## Session and transactions
- `spring.jpa.open-in-view=false`.
- `@Transactional` on services, never controllers; `readOnly = true` for reads.

## Fetching
- Default `FetchType.LAZY`; never `EAGER` as default.
- Prevent N+1 with `@EntityGraph`, `JOIN FETCH`, or projections. Verify new
  collection queries for N+1 in tests or SQL logs.
- Use projections (records where supported) when the full entity is not needed.

## Entities
- `equals`/`hashCode` on a stable id or business key; no `@Data`.
- `@Version` for optimistic locking wherever concurrent edits are possible.
- Never concatenate query strings; use named parameters.
- `BigDecimal` for money and medication dosages; never `double`/`float`.
- `Instant` or `OffsetDateTime`, stored in UTC.

## Schema and migrations
- Flyway or Liquibase manages the schema; `spring.jpa.hibernate.ddl-auto=validate`.
  Never `update`/`create` in production.
- Never edit a migration that was already applied anywhere shared; add a new
  one.
- Flyway naming: `V<next>__<snake_case_description>.sql`. One concern per
  migration.
- Make destructive changes (drop/rename) in two steps across releases unless
  the spec says otherwise, and record the risk in `CONCERNS.md`.
- When a delivery creates tables, document purpose of every table and field,
  plus behavior-affecting constraints and indexes, in the REPORT entry.
