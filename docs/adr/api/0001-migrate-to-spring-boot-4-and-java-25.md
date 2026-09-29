# ADR-0001: Migrate the API to Spring Boot 4.1 and Java 25

- **Status:** Accepted
- **Date:** 2026-09-29
- **Scope:** `api/`

## Context

The API was built on Spring Boot 3.3.4 and Java 21. Spring Boot 4 (on Spring Framework 7) is now
the current major line. Spring Boot 3.x is heading toward the end of open-source support, and
Java 25 is the current LTS release. The project is still early (one Flyway migration, a small
test suite), so upgrading now costs less than it will once more code depends on 3.x APIs.

The build already used Gradle 9.6 and Testcontainers 2.0.5, and both work with Boot 4 and
Java 25. A JDK 25 was already installed locally (SDKMAN), and Gradle's toolchain detection picks
it up without extra configuration.

## Decision

Upgrade `api/` to **Spring Boot 4.1.1** and **Java 25** (toolchain), and adapt the build and
code to Boot 4's changes.

### Build changes (`api/build.gradle.kts`)

| Before | After | Why |
|---|---|---|
| `org.springframework.boot` 3.3.4 | 4.1.1 | Target version |
| `io.spring.dependency-management` 1.1.6 | 1.1.7 | Latest; required for Boot 4 BOM |
| Toolchain Java 21 | Java 25 | Target version (class file major version 69) |
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` | Boot 4 renamed the servlet web starter |
| `spring-boot-starter-oauth2-resource-server` | `spring-boot-starter-security-oauth2-resource-server` | Boot 4 renamed the security starters |
| `org.flywaydb:flyway-core` | `spring-boot-starter-flyway` | Boot 4 moved auto-configuration into per-technology modules. Without the starter, Flyway is **not** auto-configured and migrations silently don't run |
| springdoc-openapi 2.6.0 | 3.1.1 | springdoc 2.x only supports Boot 3 |
| — | `spring-boot-resttestclient` (test) | `TestRestTemplate` moved out of `spring-boot-test` |
| — | `spring-boot-restclient` (test) | `TestRestTemplate` needs `RestTemplateBuilder`, which now lives here |

Lombok (1.18.46), Hibernate (7.4), Flyway (12.4) and Testcontainers (2.0.5) come from the
Boot 4.1.1 BOM. All of them support Java 25.

### Code changes

- **Tests:** `TestRestTemplate` moved from `org.springframework.boot.test.web.client` to
  `org.springframework.boot.resttestclient`. `@SpringBootTest` no longer registers it
  automatically, so `AbstractIntegrationTest` now has `@AutoConfigureTestRestTemplate`.
- **`JwtAuthenticationFilter`:** Spring Framework 7 deprecated `org.springframework.lang.NonNull`
  in favour of JSpecify, so the filter now uses `org.jspecify.annotations.NonNull`.

No production logic, entity mappings, security configuration or Flyway migrations had to change.

## Consequences

### Positive

- Supported, current platform: Spring Framework 7, Hibernate 7, Java 25 LTS.
- Boot 4's modular starters make it explicit which features the app pulls in.
- Null-safety annotations now match Spring's JSpecify-based model.

### Negative / things to watch

- **Missing-module failures show up at runtime, not at compile time.** Compilation succeeded
  before `spring-boot-restclient` was added, but every integration test failed at context load
  with `NoClassDefFoundError: org/springframework/boot/restclient/RestTemplateBuilder`. When you
  adopt a Boot feature, add its dedicated starter or module.
- **Jackson:** Boot 4 uses Jackson 3 (`tools.jackson`) by default. `jjwt-jackson` 0.12.6 still
  depends on Jackson 2 (`com.fasterxml.jackson`), which Boot 4.1.1 still manages (2.21.5). Both
  are on the classpath side by side. Revisit when jjwt ships Jackson 3 support or Boot drops the
  Jackson 2 BOM.
- Every developer and CI environment needs a JDK 25 available for the Gradle toolchain.

## Verification

`./gradlew clean build` passes: 21/21 tests (the 4 controller test classes and
`UserRepositoryTest`) against Testcontainers PostgreSQL 17. The logs confirm
`Spring Boot (v4.1.1)`, `using Java 25.0.3`, and `Successfully applied 1 migration`, which shows
that Flyway still auto-configures through the new starter.

## References

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [springdoc-openapi 3.x](https://springdoc.org/)
