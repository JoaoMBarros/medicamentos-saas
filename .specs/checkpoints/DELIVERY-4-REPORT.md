# Delivery 4 - Manual inventory and batch management

### Inventory and batches - 2026-11-10T16:17:34Z

**Specification and assumptions:** A batch is unique within an organization by
medication and lot number. Status is `ACTIVE` or `EXPIRED`, derived from the
expiration date. Stock can never become negative. `Idempotency-Key` is required
for batch creation and all movement writes. The expiring query accepts a
`days` window from 0 through 365 and defaults to 30 at the HTTP boundary; this
is a temporary delivery-level choice until expiration preferences are defined
in Delivery 6.

**Acceptance criteria:**

- **AC-1:** An authenticated active member can create, retrieve, update, and
  paginate batches belonging only to the current organization.
- **AC-2:** Batch creation validates medication/location ownership, dates, and
  positive quantity, and rejects duplicate organization/medication/lot keys.
- **AC-3:** Batch creation persists its initial addition movement and
  idempotency record in the same transaction.
- **AC-4:** Addition, removal, transfer, and adjustment movements update the
  batch and persist an immutable movement history in one transaction.
- **AC-5:** Removal and adjustment operations reject a negative resulting
  quantity; optimistic `@Version` rejects concurrent lost updates.
- **AC-6:** Repeating a batch or movement request with the same tenant-scoped
  idempotency key returns the existing resource without a duplicate movement.
- **AC-7:** Expired, soon-to-expire, filtered, and sorted batch queries are
  tenant-scoped.

**Delivered behavior:** Added stock batch and immutable inventory movement
entities, tenant-aware transactional services, REST endpoints, validation,
optimistic locking, idempotency persistence, expiry queries, movement history,
and Flyway migration V6. Batch location changes are required to use transfer
movements, and no deletion endpoint exists for movement history.

**Applicable business rules:** BR 1 tenant isolation; BR 4 lot ownership,
duplicate lot prevention, valid dates/quantity, transactional movement
consistency, immutable history, and optimistic concurrency; BR 5
idempotency; BR 7 expiry traceability; BR 8 authentication and auditability.

**Validation:** Inventory unit and controller tests pass with
`.\gradlew.bat test --tests "br.com.medicacao.saas.inventory.*"`. The full
`.\gradlew.bat check` executes 36 tests but remains blocked by the existing
Spring context test because Docker/Testcontainers is unavailable. Coverage
verification using only the non-container selection reports 69%, so the 85%
threshold was not weakened; the result is incomplete without the context test.

**Security / limitations:** Every endpoint is protected by the existing JWT
resource-server configuration and every service operation verifies active
membership and organization-scoped references. Movement actor IDs are stored
for audit but are not returned in API responses. The migration and optimistic
locking behavior still require execution against PostgreSQL for production
confidence; H2/Testcontainers differences are documented in CONCERNS.md.

**Endpoints (curl):**

```bash
curl -i -X POST "http://localhost:8080/api/v1/batches" \
  -H "Authorization: Bearer ******" \
  -H "Idempotency-Key: batch-2026-001" \
  -H "Content-Type: application/json" \
  -d '{"medicationId":"<medicationId>","locationId":"<locationId>","lotNumber":"LOT-001","expirationDate":"2027-12-31","quantity":100}'

curl -i "http://localhost:8080/api/v1/batches?page=0&size=20&sort=expirationDate,asc" \
  -H "Authorization: Bearer ******"

curl -i "http://localhost:8080/api/v1/batches/expired?page=0&size=20" \
  -H "Authorization: Bearer ******"

curl -i "http://localhost:8080/api/v1/batches/expiring?days=30&page=0&size=20" \
  -H "Authorization: Bearer ******"

curl -i -X POST "http://localhost:8080/api/v1/batches/<batchId>/movements" \
  -H "Authorization: Bearer ******" \
  -H "Idempotency-Key: movement-2026-001" \
  -H "Content-Type: application/json" \
  -d '{"type":"REMOVAL","quantity":2}'

curl -i "http://localhost:8080/api/v1/batches/<batchId>/movements" \
  -H "Authorization: Bearer ******"
```

**Database:**

| Table | Purpose | Notable fields / constraints / indexes |
|-------|---------|-----------------------------------------|
| `app.stock_batches` | Tenant-owned medication lots and current stock | Foreign keys to organizations, medications, and locations; unique organization/medication/lot; non-negative quantity check; status check; optimistic `version`; indexes for organization/expiration, medication, and location. |
| `app.inventory_movements` | Immutable stock movement audit history | Foreign keys to organization, batch, and optional locations; operation type check; tenant/type/idempotency unique key; actor and creation timestamp; organization/batch/created index. |
| `app.idempotency_records` | Tenant-scoped replay mapping for retriable writes | Unique organization/operation/key; resource ID points to the original batch or movement; creation timestamp supports operational retention policies. |
