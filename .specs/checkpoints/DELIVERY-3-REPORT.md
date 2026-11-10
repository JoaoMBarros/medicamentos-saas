# Delivery 3 - Medication catalog

### Medication catalog - 2026-11-10T04:12:44Z

**Goal:** provide a tenant-isolated medication catalog with normalized
identity, CRUD operations, and paginated search.

**Acceptance criteria:**

- **AC-1:** Given an authenticated active member, when a valid medication is
  created, then it is stored under the member's organization and returned as a
  response record.
- **AC-2:** Given a medication with the same normalized name, active
  ingredient, concentration, and presentation in the organization, when it is
  created or updated, then the operation returns a conflict.
- **AC-3:** Given an authenticated active member, when a medication is
  retrieved or updated, then only a medication belonging to the current
  organization can be accessed.
- **AC-4:** Given an authenticated active member, when the catalog is searched,
  then results are tenant-scoped, paginated, filtered by name/active
  ingredient/presentation, and sortable through `Pageable`.
- **AC-5:** Given an invalid external request, then Bean Validation returns the
  existing RFC 9457 validation problem response.

**Delivered behavior:** Medication persistence, tenant-aware service methods,
immutable request/response records, validation, duplicate prevention, update
with optimistic locking, and paginated filtered search were added. Medication
identity is normalized to lowercase for the four fields selected for
deduplication. Regulatory identifiers are optional `anvisaRegistration` and
`ean` fields.

**Applicable business rules:** BR 1 (organization and tenant isolation), BR 3
(medication catalog ownership and duplicate prevention), and BR 8
(authenticated protected endpoints).

**Validation:** `.\gradlew.bat test --tests
br.com.medicacao.saas.catalog.*` was attempted but could not start because the
environment has no Java executable and `JAVA_HOME` is not set. `.\gradlew.bat
check` remains pending for the same environment limitation. Unit tests cover
AC-1 through AC-4; the existing validation handler covers AC-5.

**Assumptions / limitations:** This delivery intentionally covers only the
medication catalog; locations remain in the next scope. The roadmap requires
ModelMapper, but this repository has no ModelMapper dependency or configured
mapper; mapping is kept in the catalog response mapper outside controllers
without adding an unverified dependency. The database-level uniqueness
constraint and optimistic locking require execution against the configured
database to be fully validated.

**Security checklist:** All endpoints are under the existing authenticated
request matcher. Service reads and writes require an active membership and
scope every repository lookup by `organization_id`. No medication values are
logged. Cross-tenant access is rejected by lookup scoping and membership
checks; execution of the full security test suite is pending Java availability.

**Endpoints (curl):**

```bash
curl -i -X POST "http://localhost:8080/api/v1/medications" \
  -H "Authorization: Bearer ******" \
  -H "Content-Type: application/json" \
  -d '{"name":"Tylenol","activeIngredient":"Paracetamol","concentration":"500 mg","presentation":"Tablet","anvisaRegistration":"12345","ean":"78901234"}'

curl -i "http://localhost:8080/api/v1/medications?page=0&size=20&term=para&sort=name,asc" \
  -H "Authorization: Bearer ******" \
  -H "Accept: application/json"

curl -i "http://localhost:8080/api/v1/medications/{medicationId}" \
  -H "Authorization: Bearer ******" \
  -H "Accept: application/json"

curl -i -X PUT "http://localhost:8080/api/v1/medications/{medicationId}" \
  -H "Authorization: Bearer ******" \
  -H "Content-Type: application/json" \
  -d '{"name":"Tylenol","activeIngredient":"Paracetamol","concentration":"650 mg","presentation":"Tablet","anvisaRegistration":"12345","ean":"78901234"}'
```

**Database:**

| Table | Purpose | Notable fields / constraints / indexes |
|-------|---------|-----------------------------------------|
| `app.medications` | Tenant-owned normalized medication catalog | `organization_id` references `app.organizations`; unique identity on organization plus name, active ingredient, concentration, and presentation; `version` supports optimistic locking; indexes support organization/name and organization/active ingredient searches. |
