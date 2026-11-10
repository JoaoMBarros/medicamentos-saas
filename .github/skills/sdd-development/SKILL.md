---
name: sdd-development
description: "Use for any feature, endpoint, bugfix, refactor or behavior change in a Java 21 / Spring Boot project that keeps its specs under .specs/ (BUSINESS_RULES.md, TASKS.md, CONCERNS.md, checkpoints/). Drives Specification-Driven Development: clarify requirements, write testable acceptance criteria, check business rules, implement incrementally, test against the spec, and update TASKS, REPORT, CONCERNS and the PR. Use it whenever the user asks to implement, add, fix, change or review backend behavior in this project, even if they do not say 'spec' or 'SDD'."
argument-hint: "Describe the feature, requirement, bug, refactor, or specification to develop."
user-invocable: true
---

# Specification-Driven Development (SDD)

Develop from an explicit, testable specification. Keep the specification,
implementation, tests, and documentation aligned throughout.

## How to use this skill

This file holds the **process**. Stack standards live in `references/`; read
only what the task touches, **before editing code**:

| Task touches...                                   | Read                                  |
|---------------------------------------------------|---------------------------------------|
| Any Java code (includes the Lombok rules)         | `references/java21.md`                |
| Controllers, services, DTOs, config, HTTP clients | `references/spring-api.md`            |
| Entities, repositories, queries, migrations       | `references/persistence-jpa.md`       |
| Auth, tenants, logging, health/personal data      | `references/security-compliance.md`   |
| Tests, coverage, Gradle, Docker, observability    | `references/testing-build.md`         |
| Writing a spec, REPORT, CONCERNS entry or PR text | `references/templates.md`             |

## Choose the flow

- **Feature / new endpoint**: full workflow below.
- **Bugfix**: reproduce first with a failing test that encodes the expected
  behavior (this is the acceptance criterion), fix the root cause, confirm the
  test passes, then run steps 6-7. Check `BUSINESS_RULES.md` only if the bug
  touches a business rule.
- **Refactor**: state the behavior that must NOT change, confirm existing tests
  cover it (add characterization tests if not), change structure only, and
  show the same tests pass before and after. No behavior change is allowed.

## Workflow

1. **Understand the request**
   - Identify goal, scope, constraints, assumptions, and affected components.
   - If different interpretations would change behavior, ask. If nobody can
     answer (background or autonomous run), choose the most conservative
     interpretation, record it as an explicit assumption, and carry it into the
     REPORT and PR. Never guess on a business-rule conflict (see step 5).

2. **Define the specification**
   - Describe observable behavior: inputs, outputs, validation, error cases,
     non-functional requirements.
   - Write measurable acceptance criteria (Given/When/Then; template in
     `references/templates.md`). Each criterion gets an ID (AC-1, AC-2...) that
     tests and the REPORT will reference.

3. **Inspect the codebase and specs**
   - Find existing patterns, related functionality, tests, config, docs.
   - Read `.specs/BUSINESS_RULES.md` and list the rules that apply (by ID or
     heading) in your working notes.
   - If `.specs/BUSINESS_RULES.md` does not exist: ask the user. If you cannot
     ask, state the assumption, do not invent business rules, and flag it in
     the REPORT. If `TASKS.md`, `CONCERNS.md` or the REPORT file are missing,
     create them from `references/templates.md`.
   - Reuse established abstractions unless the spec requires a deliberate change.

4. **Plan the smallest implementation**
   - Focused, reversible steps; note dependencies, migrations, compatibility
     and test needs. No speculative features, no unrelated refactoring.

5. **Implement against the specification**
   - Satisfy the acceptance criteria and enforce the applicable business rules.
   - **Stop and clarify** if a rule is undefined or conflicts with the request.
     Do not implement around it.
   - Validate external input at system boundaries; keep errors explicit.
   - Follow the matching `references/` files.
   - Update directly related tests and docs.

6. **Verify behavior**
   - Test success, invalid input, boundary, and failure scenarios for every AC.
   - Run the smallest relevant check first, then the project-required checks
     (`./gradlew check` including coverage verification).
   - Find root causes. Never weaken, skip, or delete a failing test to get green.

7. **Review, document, report**
   - Compare the result against every AC and applicable business rule.
   - Complete the Definition of Done below.
   - Final answer: changed files, commands run, results, limitations, risks.
     Separate verified facts from assumptions and suggestions.

## Definition of Done

Do not call the work complete until each item is true or explicitly marked
"not applicable" with a reason.

- [ ] Every AC is implemented and covered by at least one test (cite AC IDs).
- [ ] Applicable `BUSINESS_RULES.md` rules are enforced and tested.
- [ ] `./gradlew check` passes (tests, static analysis, coverage >= 85%).
- [ ] Security checklist passed (authn on every endpoint, tenant isolation
      tested, no personal/health data in logs).
- [ ] Each new endpoint has a working Bash `curl` example (method, URL,
      headers, auth header, sample body) in the final answer **and** in the REPORT.
- [ ] The finished activity's checkbox is ticked in `.specs/TASKS.md`
      (same change).
- [ ] `.specs/checkpoints/<DELIVERY>-REPORT.md` has a new entry: completion
      timestamp (ISO-8601, UTC, from `date -u`), delivered behavior, validation,
      limitations, `curl` examples, and, if tables were created, each table and
      field purpose plus behavior-affecting constraints and indexes.
- [ ] Any decision made only for local dev/test has a checkbox in
      `.specs/CONCERNS.md` under `Review pre development deploy` or
      `Review pre prod deploy`, added before finishing.
- [ ] No secrets, credentials, personal data, generated artifacts or unrelated
      changes in the diff.
- [ ] If a PR is opened: description covers what, why, validation results,
      known limitations and follow-ups (template in `references/templates.md`).

Before any deployment, review the matching `CONCERNS.md` category and resolve
or explicitly accept every open item.

## Engineering principles

- Prefer clarity, correctness, maintainability, and observability over clever
  shortcuts.
- Preserve existing behavior unless a change is explicitly required.
- Keep changes cohesive; do not mix unrelated concerns.
- No new dependencies, abstractions, or configuration without a concrete need.
- Use Lombok to remove boilerplate on services and entities (rules in
  `references/java21.md`); use `record` for DTOs and value objects.
- Comments explain why, not what. Javadoc only for complex or non-obvious
  methods and relevant public APIs.
- Replace magic numbers and strings with constants or enums. Put non-sensitive,
  deployment-independent values that may change in YAML configuration.
- Do not optimize prematurely; measure first.

## Response expectations

- Deliver complete, compilable, consistent Java 21 / Spring Boot 3 code with
  matching unit and integration tests.
- Call out security, performance, N+1, concurrency and data-leakage risks.
- If information is missing, ask focused questions or state explicit assumptions.
- In code reviews, list findings by severity (critical, high, medium, low), each
  with a suggested fix.

## Prohibited anti-patterns (hard stops)

- Field `@Autowired`, manually constructed Spring beans, business logic in controllers.
- JPA entities exposed through the API, default `FetchType.EAGER`,
  `ddl-auto=update|create` in production.
- Empty or generic `catch (Exception)`, `printStackTrace()`, `System.out.println`.
- `double`/`float` for dosage or money; `Date`/`Calendar`.
- Personal or health data in logs; secrets in code or Git.
- Unchecked `Optional.get()`, `null` collections, tests that depend on shared
  databases, real clocks, network, or execution order.
- Unauthenticated service endpoints.

## Extending this skill

Add new practices to the matching `references/` file (or a new one, then add it
to the table above). Keep each rule actionable and verifiable.