# AGENTS.md

Instructions for AI coding agents working in this repo. See each module's own README
(`api/README.md`, `app/README.md`) for human-facing setup docs — this file is about the things
that aren't obvious from reading the code, and that have already caused real build/test failures.

## Repo layout

```
diecast-collector-app/
├── api/   Spring Boot 3 REST API (Java 21), PostgreSQL, Google/Apple social login
└── app/   Kotlin Multiplatform + Compose Multiplatform app (Android + iOS)
```

The two modules are independent Gradle projects (separate `settings.gradle.kts`, no shared
build) that happen to live in one repo.

## api/ — Spring Boot API

**Stack:** Java 21, Spring Boot 3.3, PostgreSQL 17 + Flyway, Spring Security (stateless JWT),
Testcontainers 2.x for integration tests.

**Build & test:**
```bash
cd api
./gradlew build          # compile + test
./gradlew test           # tests only
./gradlew bootRun         # run locally (needs `docker compose up -d` first for Postgres)
```

**Before running tests, know this:**
- `app.jwt.secret` has **no default** in `application.yml` (`${APP_JWT_SECRET:}`) — this is
  intentional (no hardcoded secret in source), but it means integration tests must supply their
  own via `@DynamicPropertySource` in `AbstractIntegrationTest`. If you add a new base test class
  that boots the Spring context, it needs the same treatment or every test in it will fail with
  `WeakKeyException` before even reaching your test code.
- The shared `PostgreSQLContainer` in `AbstractIntegrationTest` is started **once**, in a static
  initializer, and never explicitly stopped (singleton container pattern). Do not add
  `@BeforeAll`/`@AfterAll` start/stop calls back in — with multiple test classes sharing that
  static field, per-class lifecycle methods stop and restart the *same* container between
  classes, and the restart races new connections (`ConnectException`, hard to diagnose).
- `automaker.name`, `brand.name`, and `collection.name` all have **unique** constraints in
  `V1__init.sql`, and (unlike `Model`) none of these three are scoped per-user. Since the test DB
  now persists across the whole suite (see above), never hardcode a fixed name for these in a
  test — two test methods (or two test classes) creating `"Ferrari"` will collide the second
  time. Suffix a `UUID.randomUUID()` per test invocation instead.
- Any REST controller endpoint not explicitly listed in `SecurityConfig`'s `permitAll()` requires
  a bearer token. `AbstractIntegrationTest.authHeaders()` persists a throwaway `User` and mints a
  real token via `AppJwtService` — use it rather than calling endpoints unauthenticated.
- Entity `@Table`/`@Column` names and the Flyway migration **must stay in sync manually** —
  `hibernate.ddl-auto: validate` only catches the mismatch at context-startup time (i.e. when a
  test runs), it won't stop you from writing one. If you add or rename a field on an entity,
  update `V1__init.sql` (or add a new migration) in the same change.
- `POST /models` returns nested `automaker`/`brand`/`collection` as bare `{id}` references, not
  fully hydrated — `ModelService.create()` never re-fetches them after save. `GET /models/{id}`
  *does* fully hydrate them via `@EntityGraph`. Don't assume the create response gives you names.

## app/ — Kotlin Multiplatform app

**Stack:** Kotlin 2.0.20, Compose Multiplatform 1.7.0, AGP 8.13.2 (bumped from 8.5.2 by Android
Studio's sync — the version in `libs.versions.toml` is the source of truth, not what's in a
commit message), Gradle 8.13, Ktor client.

**Build & test:**
```bash
cd app
./gradlew :composeApp:assembleDebug   # build a debug APK
./gradlew :composeApp:installDebug    # build + install on a connected device/emulator
```

**Before touching build config, know this:**
- Only the Android target builds from Linux. `iosArm64`/`iosSimulatorArm64`/`iosX64` are silently
  disabled at configure time (`kotlin.native.ignoreDisabledTargets`) because there's no Xcode
  toolchain here — this is expected, not a failure.
- The Gradle wrapper version and the Kotlin/AGP versions in `libs.versions.toml` are coupled.
  Kotlin 2.0.20's Gradle plugin doesn't support Gradle 9 (`NoClassDefFoundError:
  DefaultArtifactPublicationSet` when configuring `iosX64()` if you bump the wrapper past it). If
  you upgrade one, check compatibility with the other before assuming a sync failure is your code.
- If `./gradlew` is missing from `app/` (it's checked in now, but if it's ever deleted): Android
  Studio's own Gradle Tooling API doesn't need the wrapper script to sync, only
  `gradle-wrapper.properties` — but the terminal does. Regenerate with whatever Gradle Android
  Studio already downloaded, e.g.
  `~/.gradle/wrapper/dists/gradle-*/*/gradle-*/bin/gradle wrapper --gradle-version <version>`.
- `PlatformContext` (`commonMain/auth/TokenStorage.kt`) is an `expect class`, not just a type
  alias target — the Android `actual` can't be `typealias PlatformContext = Context` because
  `Context` is abstract and modality has to match. Wrap it (`actual class
  PlatformContext(val context: Context)`) instead.
- Google/Apple sign-in and iOS camera capture are intentionally stubbed with `TODO`s — don't
  "fix" these without first confirming Google/Apple OAuth credentials exist (see `api/README.md`
  auth flow) and, for iOS, that an `.xcodeproj` has been generated on a Mac.

## Local emulator setup (this environment specifically)

- Android SDK lives at `~/Android/Sdk`, AVD `Medium_Phone` is already created.
- `adb`/`emulator` binaries: `~/Android/Sdk/platform-tools/adb`, `~/Android/Sdk/emulator/emulator`.
- Launch headless: `emulator -avd Medium_Phone -no-snapshot-save`, then poll
  `adb shell getprop sys.boot_completed` until it returns `1` before installing/launching.
- The debug keystore (`~/.android/debug.keystore`) was generated manually with `keytool`, not by
  Android Studio — its SHA-1 is what's registered as the Android OAuth client's fingerprint in
  Google Cloud Console. If it's ever regenerated, the Google Cloud Console Android client needs
  its SHA-1 updated to match, or Google sign-in will fail to recognize the app.

## General conventions

- Don't add default values back into secrets (`APP_JWT_SECRET`, `GOOGLE_CLIENT_ID`,
  `APPLE_CLIENT_ID`) — they were deliberately removed from `application.yml`.
- Multi-tenancy matters: every `Model` read/write must stay scoped to `owner_id` /
  `CurrentUser.id()`. `Automaker`/`Brand`/`Collection` are intentionally global/shared, not
  per-user — don't add ownership scoping to those without discussing it first (it'd be a real
  product change, not a bug fix).
