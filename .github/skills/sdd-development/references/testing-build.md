# Testing, build and observability

## Testing
- Coverage: at least 85% line coverage from useful tests.
- Unit tests: JUnit 5, Mockito, AssertJ, no Spring context.
- Slices: `@WebMvcTest` for controllers, `@DataJpaTest` for repositories.
- Integration: `@SpringBootTest` + Testcontainers with real PostgreSQL; never
  H2 as a substitute.
- API/contract: MockMvc, `RestTestClient`, or `WebTestClient`.
- Arrange-Act-Assert; one concept per test; descriptive names. Reference AC IDs
  in test names or `@DisplayName`.
- Cover business rules, boundaries, validation, authorization, tenant
  isolation, error handling and exception flows. Do not test trivial
  getters/setters just to raise coverage.
- Deterministic: inject `Clock`; no real time, network, shared DBs, or
  order assumptions.
- Reuse the Spring test context; avoid needless `@DirtiesContext` and many
  distinct `@MockBean` sets.
- Consider PIT mutation testing when test quality must be measured.

### If the 85% gate fails
1. Find the uncovered lines. If they hold real behavior, write behavior tests.
2. If they are generated code (Lombok output is already ignored via `lombok.addLombokGeneratedAnnotation`), config classes, or the `main` class, exclude
   them in the JaCoCo config with a comment saying why.
3. Never lower the threshold or add assertion-free tests. If neither fits, stop
   and report it to the user.

## Build and quality
- Java 21 Gradle toolchain.
- Lombok configured as in `references/java21.md` (annotation processor and
  `lombok.config`).
- `jacocoTestReport` and `jacocoTestCoverageVerification` (min 85% lines)
  attached to `check`.
- Version catalog and Spring Boot BOM; do not hard-code BOM-managed versions.
- Static analysis/formatting: Checkstyle or Spotless, SpotBugs or Error Prone,
  optionally SonarQube when configured.
- Images: Buildpacks or multi-stage Dockerfile on a JRE 21 base, non-root user.
- Measure before enabling CDS, AOT, ZGC or other optimizations.

## Observability
- Parameterized SLF4J (`log.info("x {}", id)`), never concatenation. Right log
  levels; structured JSON logs in production.
- Actuator and Micrometer; OpenTelemetry tracing when integrated.
- Liveness and readiness health checks.
- Propagate a correlation/trace ID in logs and error responses.
