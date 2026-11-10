# Templates

## 1. Specification and acceptance criteria

```markdown
### Feature: <name>
**Goal:** <one sentence>
**Scope:** in: ... | out: ...
**Applicable business rules:** BR-<id> <title>, ...
**Assumptions:** <explicit, or "none">

**Inputs / Outputs / Validation:** <fields, types, limits>
**Non-functional:** <auth, performance, audit, tenant isolation>

| ID   | Given                       | When                 | Then                                  |
|------|-----------------------------|----------------------|---------------------------------------|
| AC-1 | an authenticated clinician  | POST /api/v1/x valid | 201 + Location, record persisted      |
| AC-2 | same                        | POST with empty name | 422 ProblemDetail naming the field    |
| AC-3 | a user of tenant B          | GET x of tenant A    | 404, no data leaked                   |
```

## 2. `.specs/TASKS.md` entry

```markdown
## <Delivery name>
- [ ] <activity>   <- tick to [x] in the same change that completes it
```

## 3. `.specs/CONCERNS.md` entry

```markdown
## Review pre development deploy
- [ ] <local-only decision, why it is unsafe/limited shared, what to do>

## Review pre prod deploy
- [ ] <e.g. Password Grant enabled for tests; disable and verify before prod>
```

## 4. `.specs/checkpoints/<DELIVERY>-REPORT.md` entry

```markdown
### <Feature name> - <YYYY-MM-DDTHH:MM:SSZ (UTC)>
**Delivered behavior:** <high level, 2-5 lines, mapped to AC-1..n>
**Validation:** `./gradlew check` -> passed; coverage <n>%; <other checks>
**Assumptions / limitations:** <or "none">

**Endpoints (curl):**
```bash
curl -X POST "http://localhost:8080/api/v1/<resource>" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"field": "value"}'
```

**Database (only if tables were created):**
| Table | Purpose | Notable fields / constraints / indexes |
|-------|---------|-----------------------------------------|
```

## 5. Curl example (final answer for each new endpoint)

```bash
curl -i -X GET "http://localhost:8080/api/v1/<resource>?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"
```

## 6. Pull request description

```markdown
## What
<what is being merged>

## Why
<problem or requirement, link to spec/task>

## Validation
- `./gradlew check`: <result>, coverage <n>%
- Acceptance criteria covered: AC-1..n

## Known limitations / follow-ups
- <item, or "none"> (see .specs/CONCERNS.md)
```
