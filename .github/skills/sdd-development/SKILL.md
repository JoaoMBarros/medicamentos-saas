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
   - Reuse established abstractions and conventions unless the specification
     requires a deliberate change.

4. **Plan the smallest implementation**
   - Break the work into focused, reversible changes.
   - Identify dependencies, migration needs, compatibility risks, and test
     coverage requirements.
   - Avoid speculative features and unrelated refactoring.

5. **Implement against the specification**
   - Make precise changes that satisfy the acceptance criteria.
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

## Extending this skill

Add new domain-specific practices under focused sections. Keep each practice
actionable, testable, and consistent with the workflow above. Prefer linking
additional reference files from this skill when the content becomes extensive.
