---
name: create-commit
description: "Create a clear and concise English commit following the feature or fix pattern, using the current branch name and reviewing the staged changes before committing."
argument-hint: "Describe the change to commit, or ask the skill to infer it from the current diff."
user-invocable: true
---

# Create commit

## Goal

Prepare and create a commit with a Conventional Commits subject in English. Use
the current branch name and the actual change description to produce a precise,
concise message.

## Procedure

1. Check the current branch, working tree, staged files, and recent commit style.
2. Inspect the diff and confirm that all staged changes belong to the requested
   change. Do not stage unrelated files.
3. Classify the change:
   - Use `feat` for a new capability or behavior.
   - Use `fix` for a correction to existing behavior.
4. Derive a short lowercase scope from the branch name when it is meaningful.
   Remove branch prefixes such as `feature/`, `feat/`, `fix/`, and `bugfix/`.
5. Write the subject in English using this format:

   ```text
   <type>(<scope>): <imperative description>
   ```

   Omit the scope only when no useful scope can be derived. Keep the subject
   clear, concise, and no longer than 72 characters when practical.
6. Run the smallest relevant validation command before committing. Do not claim
   validation passed when the required runtime or tooling is unavailable.
7. Show the proposed commit message and a concise summary of the staged diff.
8. Commit only after the user explicitly confirms, unless the user directly
   requested the commit in the current message.

## Rules

- Never include secrets, credentials, generated artifacts, or unrelated changes.
- Do not rewrite, amend, or squash existing commits.
- Use an English imperative verb such as `add`, `implement`, `update`, or
  `fix`.
- Use `feat` or `fix` only; ask for clarification if the change does not fit
  either category.
- Keep the commit focused: one logical change per commit.
