# Diecast Collector API (v2)

Spring Boot 4 REST API for the diecast collector mobile app, replacing the earlier Micronaut
prototype in `diecast-collector-api` (left untouched as a reference).

## Stack

- Java 25 (LTS), Spring Boot 4.1
- PostgreSQL 17, Flyway migrations
- Spring Data JPA (Hibernate)
- Spring Security, stateless, app-issued JWT sessions
- Social login: Google Sign-In and Sign in with Apple (ID tokens verified server-side against
  each provider's published JWKS — see `security/GoogleTokenVerifier` and `AppleTokenVerifier`)

## What's here vs. the old API

- Every model is now owned by a `User` (`owner_id`) — this is a personal collection tracker, so
  all `/models` reads/writes are scoped to the authenticated caller.
- `Model.automaker` / `.brand` / `.collection` were fixed from `@OneToOne` to `@ManyToOne` (the
  old mapping was semantically wrong — many models share one automaker/brand/collection — and,
  because the associations are nullable, a unidirectional `@OneToOne(fetch = LAZY)` can't
  actually be lazy in Hibernate without bytecode enhancement).
- New fields on `Model` to support brands beyond the original car-centric shape: `condition`,
  `seriesNumber` (position within the Series as printed by the brand, e.g. "10/10"; the Series
  itself is a shared `Series` reference), `chase` (generic stand-in for brand-specific rare-variant flags like Treasure
  Hunt), `purchasePrice` / `purchaseDate` / `purchasedFrom`, `notes`, and `photoUrl`.
- A photo upload endpoint (`POST /models/{id}/photo`, multipart) storing to local disk for now
  (`PhotoStorageService`) — swap for S3/GCS later without touching controllers or the schema.

## Running locally

```bash
docker compose up -d          # starts Postgres
./gradlew bootRun             # (run `gradle wrapper` once first to generate the wrapper)
```

Flyway runs the migrations in `src/main/resources/db/migration` automatically on startup.

## Auth flow

1. Mobile app signs the user in with the native Google or Apple SDK and gets an ID token.
2. App calls `POST /auth/google` or `POST /auth/apple` with `{ "idToken": "..." }`.
3. API verifies the token's signature/issuer/audience against the provider's JWKS, upserts a
   `User` row, and returns `{ accessToken, user }`.
4. App sends `Authorization: Bearer <accessToken>` on every subsequent call. The API's own JWT
   (`AppJwtService`) is what's checked from then on — the raw Google/Apple token is never reused.

Before this can work against real accounts you'll need to set:
- `GOOGLE_CLIENT_ID` — your Google OAuth client ID (the "audience" the ID token must carry)
- `APPLE_CLIENT_ID` — your Sign in with Apple services ID
- `APP_JWT_SECRET` — a real random secret (`openssl rand -base64 64`) for anything beyond local dev

## Known gaps / next steps

- No pagination or filtering on `/models` yet — fine at personal-collection scale, worth adding
  filters (by automaker/brand/series/scale) once the app needs to browse a large collection.
- Photo storage is local disk only; fine for a single-instance deployment, not for anything
  scaled horizontally or ephemeral (e.g. most PaaS containers) — move to object storage before
  deploying anywhere the filesystem isn't persistent.
- No refresh-token flow — the app JWT just expires after `app.jwt.access-token-ttl-minutes` and
  the app re-runs the social login silently using the provider SDK's cached session.
