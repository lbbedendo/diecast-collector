# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

An app for diecast collectors to track their collection of scale models. Collectors sign in with
Google or Apple, then catalogue each piece: its real-world automaker, the diecast brand that
produced it, scale/condition, series details, purchase info, and a camera photo. Each user's
collection is private to them.

Two independent Gradle projects in one repo, no shared build:

```
diecast-collector-app/
├── api/   Spring Boot 4 REST API (Java 25), PostgreSQL, Google/Apple social login
└── app/   Kotlin Multiplatform + Compose Multiplatform app (Android + iOS)
```

A sibling directory `../diecast-collector-api` is the earlier Micronaut prototype this replaces —
kept only as a reference, not part of this repo.

**See [AGENTS.md](AGENTS.md) for a list of non-obvious pitfalls already hit in this repo**
(test infra gotchas, Gradle/Kotlin/AGP version coupling, etc.) — this file covers commands and
architecture, AGENTS.md covers "things that will burn you if you don't know them."

## Commands

### api/ (Spring Boot)

```bash
cd api
docker compose up -d                 # start Postgres (only external dependency)
source .env && ./gradlew bootRun     # run the API locally, port 8080
./gradlew build                      # compile + run all tests
./gradlew test                       # tests only
./gradlew test --tests "com.diecastcollector.api.controller.AutomakerControllerTest"            # one test class
./gradlew test --tests "com.diecastcollector.api.controller.AutomakerControllerTest.createAutomaker"  # one test method
```

`.env` (gitignored, not committed) holds `APP_JWT_SECRET` and `GOOGLE_CLIENT_ID` — `application.yml`
has no default for either, so `bootRun` fails fast with `WeakKeyException` if it isn't sourced first.
`./gradlew build`/`test` don't need it: integration tests supply their own secret via
`AbstractIntegrationTest`'s `@DynamicPropertySource`.

Flyway runs migrations (`src/main/resources/db/migration`) automatically on startup. Integration
tests boot a real Postgres via Testcontainers — no separate test DB setup needed, but Docker must
be running.

### app/ (Kotlin Multiplatform)

```bash
cd app
./gradlew :composeApp:assembleDebug   # build a debug APK
./gradlew :composeApp:installDebug    # build + install on a connected device/emulator
```

No automated tests exist on the app side yet. Only the Android target builds outside macOS — iOS
targets require Xcode and are expected to be skipped here. Open in Android Studio to run/debug
interactively; see `app/README.md` for emulator networking (`10.0.2.2:8080` reaches the local API).

## Architecture

### api/ — layered Spring Boot app

`Controller → Service → Repository → Domain entity`, one of each per resource
(`Automaker`, `Brand`, `Collection`, `Model`; `User` has no controller — only created via social
login). Controllers map DTO records (`dto/*Request`, `dto/*Response`) to/from domain entities;
services hold the only business logic and own `@Transactional` boundaries; repositories are plain
Spring Data JPA interfaces.

**Data model:** `Model` is the only per-user resource (`owner_id`, scoped via `CurrentUser.id()`
on every read/write). `Automaker`, `Brand`, and `Collection` are shared/global reference data
with no ownership — don't add per-user scoping to those without it being a deliberate product
decision. `Model` has `@ManyToOne` references to all three, plus brand-agnostic fields
(`condition`, `seriesName`/`seriesNumber`, `chase`, `purchasePrice`/`purchaseDate`/`purchasedFrom`,
`notes`, `photoUrl`) so any diecast brand's pieces fit the same schema.

**Auth is two-layer, not one JWT:** the mobile app signs in with the native Google/Apple SDK to
get a provider ID token; `POST /auth/google` or `/auth/apple` verifies that token's signature
against the provider's published JWKS (`GoogleTokenVerifier`/`AppleTokenVerifier`), upserts a
`User`, and issues the API's **own** short-lived JWT (`AppJwtService`). Every other endpoint
checks that app-issued JWT via `JwtAuthenticationFilter`/`CurrentUser` — the original provider
token is never reused past the login call. `SecurityConfig` permits `/auth/**`, `/photos/**`,
`/actuator/health`, and swagger unauthenticated; everything else requires the bearer token.

**Error handling:** `GlobalExceptionHandler` (`@RestControllerAdvice`) is the single place that
maps exceptions to HTTP status — `ResourceNotFoundException` → 404, `InvalidTokenException` → 401,
bean validation failures → 400. Services throw `ResourceNotFoundException` directly rather than
returning `Optional`/null up to controllers.

**Schema is hand-synced, not generated:** entities are the source of truth for shape, but
`db/migration/V1__init.sql` must be kept in sync by hand — `hibernate.ddl-auto: validate` only
*detects* drift at context-startup time, it doesn't fix or generate anything.

### app/ — Kotlin Multiplatform + Compose

Single-activity, single-`ViewModel` architecture — no navigation library. `App.kt` holds a small
`sealed interface Screen` (`Login` / `List` / `NewModel`) and switches on it directly; `AppViewModel`
exposes one `StateFlow<AppUiState>` that every screen reads, and all screens are stateless
Composables driven by that state plus callbacks.

**Platform-specific code uses Kotlin's `expect`/`actual`**, one pair per concern, each with its own
file suffix convention: `TokenStorage` (secure storage), `SignInLaunchers` (Google/Apple SDK
entry points), `CameraCapture`, `HttpClientEngine` (Ktor engine per platform — OkHttp on Android,
Darwin on iOS). `commonMain` declares the `expect`, `androidMain`/`iosMain` provide the `actual`.

**Network layer:** `DiecastApi` (commonMain) wraps a Ktor `HttpClient` with one method per API
endpoint; `authTokenProvider` is a closure re-read on every request (not cached), so a token
refresh or logout takes effect on the very next call with no extra wiring.

## Known gaps (intentional — see the two READMEs for the full list)

- Google/Apple sign-in and iOS camera capture are stubbed with `TODO`s — they need API
  credentials or an Xcode project that don't exist in this environment, not a code fix.
- No pagination/filtering on `/models`, no refresh-token flow, local-disk-only photo storage.
- Android has no launcher icon yet (manifest omits `android:icon`).
