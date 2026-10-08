# Diecast Collector App

An app for diecast collectors to track their collection of scale models. Collectors sign in
with Google or Apple, then catalogue each piece they own: its real-world automaker, the diecast
brand that produced it, its scale and condition, series details, what they paid and where, and a
photo taken with the phone's camera. Each user's collection is private to them, so the app can
serve many collectors at once.

It's made up of a Kotlin Multiplatform mobile app (Android + iOS) backed by a Spring Boot REST API.

## Layout

```
diecast-collector-app/
├── api/   Spring Boot 4 REST API (Java 25), PostgreSQL, Google/Apple social login
└── app/   Kotlin Multiplatform + Compose Multiplatform app (Android + iOS)
```

See `api/README.md` and `app/README.md` for how to run each half.

## Quickstart

```bash
cd api && docker compose up -d && ./gradlew bootRun   # API on :8080
# in another shell:
cd app                                                 # open in Android Studio, run composeApp
```

## Data model

A `Model` is a single diecast piece in someone's collection. It belongs to the `User` created on
their first social login, and has many-to-one references to an `Automaker` (the real-world car
maker, e.g. Honda) and a `Series` (e.g. "HW Starting Grid" 2026). Each Series belongs to a `Brand`
(the diecast maker, e.g. Hot Wheels), and that is where a Model's Brand comes from. Automakers,
brands and series are shared across all users; models are scoped to their owner.

Brands differ in *how they describe a piece*, so `Model` uses generic fields rather than
brand-specific ones: `condition`, `seriesNumber` (the piece's position in its Series as
printed, e.g. "10/10"), a `chase` flag standing in for brand-specific "rare variant" names (Treasure Hunt,
Super, Premium, ...), plus `purchasePrice` / `purchaseDate` / `purchasedFrom`, `notes`, and
`photoUrl` for the camera-capture flow. A Hot Wheels, Matchbox, or California Collectibles piece
all use the same fields.

## Known gaps (see the two READMEs for the full list)

- Google/Apple sign-in and iOS camera capture are stubbed with TODOs — they need either API
  credentials that don't exist yet or an Xcode project (this was scaffolded in a Linux
  container with no Mac/Xcode available to generate and verify one).
- No pagination/filtering on `/models` yet, no refresh-token flow, local-disk photo storage only.
- Android needs a real launcher icon before shipping (manifest omits `android:icon` for now).
