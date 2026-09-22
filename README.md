# Diecast Collector App

A ground-up rebuild of the diecast-collection tracker: a Kotlin Multiplatform mobile app backed
by a new Spring Boot API, replacing the earlier Micronaut prototype (`../diecast-collector-api`,
left untouched).

## Layout

```
diecast-collector-app/
├── api/   Spring Boot 3 REST API (Java 21), PostgreSQL, Google/Apple social login
└── app/   Kotlin Multiplatform + Compose Multiplatform app (Android + iOS)
```

See `api/README.md` and `app/README.md` for how to run each half.

## Quickstart

```bash
cd api && docker compose up -d && ./gradlew bootRun   # API on :8080
# in another shell:
cd app                                                 # open in Android Studio, run composeApp
```

## Design decisions carried over from the review of the old API

The old Micronaut API (`../diecast-collector-api`) had a `Model` entity with `@OneToOne`
relations to `Automaker`/`Collection`/`Brand` that were really many-to-one, and no per-user
ownership (it was single-collection, not multi-tenant). Both are fixed here: `Model` uses
`@ManyToOne`, and every model now belongs to a `User` created on first social login, since the
app now needs to support more than one person's collection.

## What "supporting multiple brands" changed on the data model

`Automaker` (the real-world car maker, e.g. Honda) and `Brand` (the diecast brand, e.g. Hot
Wheels) already existed as separate concepts in the old schema — that part was already
brand-agnostic. What was missing was room for how brands differ in *how they describe a piece*:
`Model` gained `condition`, `seriesName` / `seriesNumber` (a brand's own wave/card numbering),
a generic `chase` flag standing in for brand-specific "rare variant" names (Treasure Hunt, Super,
Premium, ...), plus `purchasePrice` / `purchaseDate` / `purchasedFrom`, `notes`, and `photoUrl`
for the camera-capture flow. None of this is brand-specific by name, so a Hot Wheels, Matchbox,
or California Collectibles piece all use the same fields.

## Known gaps (see the two READMEs for the full list)

- Google/Apple sign-in and iOS camera capture are stubbed with TODOs — they need either API
  credentials that don't exist yet or an Xcode project (this was scaffolded in a Linux
  container with no Mac/Xcode available to generate and verify one).
- No pagination/filtering on `/models` yet, no refresh-token flow, local-disk photo storage only.
- Android needs a real launcher icon before shipping (manifest omits `android:icon` for now).
