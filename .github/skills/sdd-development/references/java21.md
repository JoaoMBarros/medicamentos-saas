# Java 21 idioms

## Types and data
- Use `record` for DTOs, requests, responses, value objects, events. Validate
  invariants in the compact constructor.
- Never use a `record` as a JPA entity (needs no-arg constructor, mutability).
- Use `sealed` interfaces/classes for closed hierarchies (operation results,
  domain errors, prescription states).
- Prefer immutable classes and `final` fields; avoid unnecessary setters.
- Prefer `List.of`, `Map.of`, `Set.copyOf`, `Stream.toList()`.
- Never return `null` for collections; return an empty one.

## Control flow
- Prefer pattern-matching `switch` and record patterns over `if`/`instanceof`
  chains and casts. Use `instanceof` with binding.
- When switching over a sealed type, never add `default`; let the compiler
  enforce exhaustiveness.

## Local variables (`var`)
Use `var` for locals only when the right-hand side makes the type obvious
(`new`, constructor call, clearly-named factory). Use the explicit type
otherwise. Never use `var` for fields, parameters, or return types.

## Other
- Text blocks (`"""`) for long JSON, SQL, JPQL.
- Sequenced collection methods (`getFirst()`, `getLast()`, `reversed()`) when
  they clarify intent.
- `java.time` only; never `Date` or `Calendar`. Inject `Clock` for
  time-dependent logic.
- `Optional` only as a return type, never in fields, parameters, or collections.
  Never `.get()` unchecked; use `orElseThrow`, `map`, `ifPresentOrElse`.

## Lombok
Lombok is part of the project toolchain. Use it to remove boilerplate, but
never to hide design decisions. Records replace Lombok for DTOs and value
objects, so do not use Lombok on records.

### Setup
- Gradle: `compileOnly` + `annotationProcessor` for `org.projectlombok:lombok`
  (and the same for `testCompileOnly`/`testAnnotationProcessor` if tests use it).
  Version comes from the Spring Boot BOM.
- Root `lombok.config`:
  ```
  config.stopBubbling = true
  lombok.addLombokGeneratedAnnotation = true
  lombok.extern.findbugs.addSuppressFBWarnings = true
  ```
  `addLombokGeneratedAnnotation` makes JaCoCo ignore generated code, so the
  85% gate measures real behavior.

### By kind of class
| Class kind            | Use                                                              | Do not use |
|-----------------------|------------------------------------------------------------------|------------|
| Service / component   | `@Slf4j`, `@RequiredArgsConstructor` (with `final` fields)        | field `@Autowired`, `@Data` |
| JPA entity            | `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@ToString(onlyExplicitlyIncluded = true)` | `@Data`, `@Setter` on the class, `@EqualsAndHashCode`, `@Builder` on the class, `@AllArgsConstructor` public |
| Config / properties   | records with `@ConfigurationProperties`                           | Lombok |
| DTO / value object    | `record`                                                         | Lombok |
| Non-entity class with many optional fields (e.g. query filter, test data) | `@Builder` (+ `@Value` for immutability) | `@Data` |
| Domain exception      | `@Getter` if it carries data                                      | `@Data` |

### Entity rules
- Expose state changes through intention-revealing methods (`activate()`,
  `changeDosage(...)`) that enforce invariants, not through setters.
- If a builder is needed to create an entity, put `@Builder` on a private or
  package-private constructor, not on the class, and keep invariant checks in
  that constructor.
- Write `equals`/`hashCode` by hand on the id or business key.
- `@ToString` on entities includes only explicit, non-sensitive fields: never
  names, national IDs, diagnoses, or medications (they would leak into logs),
  and never lazy associations (they trigger queries).
- `@Slf4j` loggers still follow the parameterized-logging rule.

### Other
- `@Value` (Lombok) only for small immutable non-record classes; prefer `record`.
- No `@SneakyThrows`, `@Cleanup`, `@Synchronized`, `val`, or `@UtilityClass`
  without a concrete reason noted in the PR.
- `@With` and `@SuperBuilder` need a concrete reason noted in the PR.

## Concurrency and virtual threads
- Default: virtual threads OFF.
- Enable `spring.threads.virtual.enabled=true` only when ALL hold:
  1. the workload is I/O-bound;
  2. a before/after load test shows improved throughput or p95 latency;
  3. a run with `-Djdk.tracePinnedThreads=full` shows no pinning hot spots;
  4. DB pool and external-API concurrency are bounded (see below).
  Record the evidence in the REPORT.
- Java 21: avoid blocking I/O inside `synchronized` (pins the carrier thread);
  use `ReentrantLock`.
- Avoid heavy `ThreadLocal` state with virtual threads.
- Do NOT use `ScopedValue` or other preview APIs; they require `--enable-preview`
  in Java 21. Only with explicit user approval.
- Never pool virtual threads; use `Executors.newVirtualThreadPerTaskExecutor()`.
- Bound scarce resources (DB connections, external APIs) with `Semaphore` or a
  correctly sized pool.
- No virtual threads for CPU-bound work.
