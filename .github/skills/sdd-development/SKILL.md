---
name: sdd-development
description: "Use for Specification-Driven Development (SDD): clarify requirements, define behavior and acceptance criteria, implement incrementally, test against the specification, and validate the final result."
argument-hint: "Describe the feature, requirement, bug, or specification to develop."
user-invocable: true
---

# Specification-Driven Development

## Objective

Develop software from an explicit, testable specification. Keep the
specification, implementation, tests, and documentation aligned throughout the
work.

## Workflow

1. **Understand the request**
   - Identify the user goal, scope, constraints, assumptions, and affected
     components.
   - Ask for clarification when different interpretations would change the
     implementation or behavior.

2. **Define the specification**
   - Describe the expected behavior in clear, observable terms.
   - Define inputs, outputs, validation rules, error cases, and non-functional
     requirements.
   - Write measurable acceptance criteria using examples when useful.

3. **Inspect the codebase**
   - Find existing patterns, related functionality, tests, configuration, and
     documentation before editing.
   - Read `.specs/BUSINESS_RULES.md` before implementing a new feature and
     identify the business rules that apply to the requested behavior.
   - Reuse established abstractions and conventions unless the specification
     requires a deliberate change.

4. **Plan the smallest implementation**
   - Break the work into focused, reversible changes.
   - Identify dependencies, migration needs, compatibility risks, and test
     coverage requirements.
   - Avoid speculative features and unrelated refactoring.

5. **Implement against the specification**
   - Make precise changes that satisfy the acceptance criteria.
   - Preserve and enforce the applicable rules from `.specs/BUSINESS_RULES.md`;
     if the feature exposes an undefined or conflicting rule, stop and clarify
     it before implementing.
   - Validate external input at system boundaries.
   - Keep responsibilities separated and errors explicit.
   - Update directly related tests and documentation.

6. **Verify behavior**
   - Test the acceptance criteria, including success, invalid input, boundary,
     and failure scenarios.
   - Run the smallest relevant validation first, then the project-required
     checks.
   - Investigate failures to their root cause; never hide or weaken a failing
     test to obtain a passing result.

7. **Review and report**
   - Compare the final implementation with every acceptance criterion.
   - Report changed files, validation commands, results, limitations, and any
     remaining risks.
   - Distinguish verified facts from assumptions or follow-up suggestions.

## Engineering principles

- Prefer clarity, correctness, maintainability, and observability over clever
  shortcuts.
- Preserve existing behavior unless a behavioral change is explicitly required.
- Keep changes cohesive and avoid mixing unrelated concerns.
- Do not introduce dependencies, abstractions, or configuration without a
  concrete need.
- Never commit secrets, credentials, personal data, generated artifacts, or
  unrelated changes.

## Project development rules

- Keep the transport layer immutable by using Java `record` types for request
  and response models.
- Use ModelMapper-based mappers for transformations between transport models,
  domain objects, and persistence models. Keep mapping logic out of controllers.
- Add Javadoc only when a method is complex, deviates from the established
  pattern, or has behavior that needs explanation. Do not document obvious
  methods.
- Store non-sensitive, deployment-independent values that may change in YAML
  configuration files instead of hard-coding them in service code.
- Protect every microservice with authentication. Do not expose service
  endpoints without the required authentication configuration and tests.
- Use an external OIDC provider, such as Keycloak, as the Authorization
  Server. Client applications must use OAuth 2.0 Authorization Code with PKCE;
  the Spring Boot application remains a Resource Server.
- Keep Password Grant limited to local development or automated tests. Never
  use it as the production authentication flow.
- Whenever a new endpoint is created, return a working Bash `curl` example for
  it, including the HTTP method, URL, required headers, authentication header
  when applicable, and a representative request body when applicable.
- Whenever an activity is completed, mark the corresponding checkbox as
  completed in `.specs/TASKS.md` in the same change.
- Whenever a new feature is implemented, add a concise, high-level entry to
  the corresponding `.specs/checkpoints/<DELIVERY>-REPORT.md` in the same
  change. Include the completion date and time,
  delivered behavior, relevant validation, and notable limitations when
  applicable.
- Every delivery entry in the appropriate `.specs/checkpoints/*-REPORT.md` must include the pertinent Bash
  `curl` examples for its externally observable endpoints or flows.
- When a delivery creates database tables, document in the corresponding
  `.specs/checkpoints/*-REPORT.md` entry the high-level purpose of every created table and
  field, as well as constraints and indexes when they affect behavior.
- Whenever a decision is made only for local development or testing, add a
  checkbox task to the appropriate category in `.specs/CONCERNS.md` before
  completing the activity. Use `Review pre development deploy` for shared
  development deployment concerns and `Review pre prod deploy` for production
  concerns.
- Before a development or production deployment, review the corresponding
  `.specs/CONCERNS.md` category and resolve or explicitly accept its open
  items.
- Whenever a pull request is opened, include a clear description of what is
  being merged, why it is being merged, the relevant validation results, and
  any known limitations or follow-up concerns.

## Extending this skill

Add new domain-specific practices under focused sections. Keep each practice
actionable, testable, and consistent with the workflow above. Prefer linking
additional reference files from this skill when the content becomes extensive.
