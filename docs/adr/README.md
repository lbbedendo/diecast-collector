# Architectural Decision Records

Records of significant architectural decisions, kept separately for each module:

- [`api/`](api/) — Spring Boot REST API
- [`app/`](app/) — Kotlin Multiplatform mobile app

Each module numbers its ADRs independently (`NNNN-short-title.md`). Every ADR follows the same
structure: **Context**, **Decision**, **Consequences**. Once an ADR is accepted, don't rewrite
it. If a decision changes, add a new ADR that supersedes the old one and mark the old one's
status as `Superseded by ADR-NNNN`.

## API

| ADR | Title | Status |
|---|---|---|
| [0001](api/0001-migrate-to-spring-boot-4-and-java-25.md) | Migrate the API to Spring Boot 4.1 and Java 25 | Accepted |
| [0002](api/0002-one-model-per-physical-copy.md) | One Model per physical copy, with no quantity field | Accepted |

## App

_No ADRs yet._
