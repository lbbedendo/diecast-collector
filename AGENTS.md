# AGENTS.md

Instructions for AI coding agents working in this repo. See each module's own README
(`api/README.md`, `app/README.md`) for human-facing setup docs — this file is about the things
that aren't obvious from reading the code, and that have already caused real build/test failures.

## Repo layout

```
diecast-collector-app/
├── api/   Spring Boot 4 REST API (Java 25), PostgreSQL, Google/Apple social login
└── app/   Kotlin Multiplatform + Compose Multiplatform app (Android + iOS)
```

The two modules are independent Gradle projects (separate `settings.gradle.kts`, no shared
build) that happen to live in one repo.

## api/ — Spring Boot API

**Stack:** Java 25, Spring Boot 4.1, PostgreSQL 17 + Flyway, Spring Security (stateless JWT),
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
- `automaker.name` and `brand.name` have **unique** constraints in `V1__init.sql`, and (unlike
  `Model`) neither they nor `series` are scoped per-user. Since the test DB
  now persists across the whole suite (see above), never hardcode a fixed name for these in a
  test — two test methods (or two test classes) creating `"Ferrari"` will collide the second
  time. Suffix a `UUID.randomUUID()` per test invocation instead. This now applies even to a
  single test on a fresh DB: `V6__seed_automakers_and_brands.sql` seeds ~270 real automakers and
  ~90 brands (Ferrari, Hot Wheels, ...), so creating any of those names fails on the first try.
- Any REST controller endpoint not explicitly listed in `SecurityConfig`'s `permitAll()` requires
  a bearer token. `AbstractIntegrationTest.authHeaders()` persists a throwaway `User` and mints a
  real token via `AppJwtService` — use it rather than calling endpoints unauthenticated.
- Entity `@Table`/`@Column` names and the Flyway migration **must stay in sync manually** —
  `hibernate.ddl-auto: validate` only catches the mismatch at context-startup time (i.e. when a
  test runs), it won't stop you from writing one. If you add or rename a field on an entity,
  update `V1__init.sql` (or add a new migration) in the same change.
- Spring Boot 4 split test support into per-technology modules. `TestRestTemplate` now lives in
  `org.springframework.boot.resttestclient` and needs **both** `spring-boot-resttestclient` and
  `spring-boot-restclient` on the test classpath, plus `@AutoConfigureTestRestTemplate` (already on
  `AbstractIntegrationTest`). Missing `spring-boot-restclient` fails every test at context load with
  `NoClassDefFoundError: org/springframework/boot/restclient/RestTemplateBuilder`.
- `POST /models` returns nested `automaker`/`series` as bare `{id}` references (`series.brand` is
  `null`), not fully hydrated — `ModelService.create()` never re-fetches them after save.
  `GET /models/{id}` *does* fully hydrate them, `series.brand` included, via `@EntityGraph`. Don't assume the create response gives you names.
- `SecurityConfig` permits `/error` explicitly, and that's load-bearing: when a request hits a
  genuinely unmapped path, Boot's default error handling forwards it internally to `/error`, and
  *that* forwarded request is itself subject to `anyRequest().authenticated()` — without the
  explicit permit, every 404 gets reported as a misleading 403 instead. Found via a test that hit
  an intentionally-unmapped path under a `permitAll()` prefix; nothing had exercised that before.
- Swagger UI is at `/swagger-ui/index.html` (`/swagger-ui.html` redirects there). `OpenApiConfig`
  requires the bearer JWT on **every** operation by default, which is what gives Swagger UI its
  "Authorize" button. A public endpoint therefore needs two changes: its path in `SecurityConfig`'s
  `permitAll()`, **and** an empty `@SecurityRequirements` on the controller (as on `AuthController`
  and `PhotoController`). Otherwise the docs show a padlock on a route that doesn't need a token.
  `SwaggerTest` checks `/auth/google` and `/photos/{filename}` stay public in the docs.
- A local `docker compose up -d` Postgres can go stale across sessions: the named volume
  (`diecast-collector-data`) persists even after `docker compose down`, so if `V1__init.sql`
  changes after you've run `bootRun` once, the next `bootRun` fails with a Flyway checksum
  mismatch (`Migration checksum mismatch for migration version 1`). Testcontainers-based
  integration tests never hit this (fresh DB every run) — only a manual local `bootRun` can. Fix
  by dropping that volume (`docker compose down -v`) if you're sure there's nothing worth keeping
  in it, never by editing the checksum in `flyway_schema_history` directly.
- `DevAuthController` (`/auth/dev`) mints a real JWT for a fixed throwaway user with no ID token
  check at all — it exists purely so the mobile app can be exercised against a local API before
  Google/Apple sign-in is wired up. It's gated by `@ConditionalOnProperty` on the *bean itself*
  (`app.auth.dev-login.enabled`, default `false`), not an in-method check — so the route simply
  doesn't exist unless `APP_AUTH_DEV_LOGIN_ENABLED=true` is set. Never flip that default, and
  remove this controller entirely once real sign-in replaces it (see the `TODO`s on the app side:
  `LoginScreen`'s "Dev login" button, `AppViewModel.devLogin()`, `DiecastApi.devLogin()`).

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
- Kotlin 2.0.20's Gradle plugin can't parse a Java 25 version string: with JDK 25 as the shell's
  default (which `api/` needs), `./gradlew :composeApp:assembleDebug` fails with the bare message
  `What went wrong: 25.0.4`. Build `app/` with JDK 21 instead, e.g.
  `JAVA_HOME=~/.sdkman/candidates/java/21.0.12+1.1-tem ./gradlew :composeApp:assembleDebug`.
- `ScaleParityTest` (`composeApp/src/androidUnitTest`) reads the API's `ModelScale.java` straight
  from `../../api/src/...` to check the app's `Scale` enum matches it, because the two Gradle
  projects can't reference each other. Adding a scale on one side fails that test until the other
  side matches, which is intended. Moving or renaming `ModelScale.java` also breaks it: update the
  path in the test.
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
- `API_BASE_URL` (`HttpClientConfig.kt`) points at `http://10.0.2.2:8080`, the Android emulator's
  alias for the host — this only works in the emulator; a physical device needs your host's LAN
  IP instead. Plaintext HTTP to that address requires the debug-only manifest override at
  `composeApp/src/debug/AndroidManifest.xml` (`usesCleartextTraffic`) — note the path is AGP's
  standard `src/debug/`, **not** the KMP-layout-v2 `src/androidDebug/` convention used for Kotlin
  sources; AGP doesn't look there for manifests even though `kotlin.mpp.androidSourceSetLayoutVersion=2`
  is set in `gradle.properties`.
- There's no real sign-in flow wired up at all yet (see above), so `LoginScreen` has a "Dev login
  (local only)" button calling `AppViewModel.devLogin()` → `DiecastApi.devLogin()` →
  `POST /auth/dev` (see the matching API-side note on `DevAuthController`). It's a visible,
  always-present button rather than something built-type-gated, because the real safety boundary
  is server-side: against any backend without dev-login enabled, it just 404s.

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
  `CurrentUser.id()`. `Automaker`/`Brand`/`Series` are intentionally global/shared, not
  per-user — don't add ownership scoping to those without discussing it first (it'd be a real
  product change, not a bug fix).
