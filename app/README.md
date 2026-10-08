# Diecast Collector — Mobile App

Kotlin Multiplatform + Compose Multiplatform app: one shared UI codebase targeting Android and
iOS.

## Layout

- `composeApp/src/commonMain` — shared UI (Compose), data models, the Ktor API client, and
  `expect` declarations for the few things that must be platform-native (secure token storage,
  camera capture, Google/Apple sign-in launchers).
- `composeApp/src/androidMain` — Android `actual`s, `MainActivity`, manifest.
- `composeApp/src/iosMain` — iOS `actual`s and the `MainViewController()` Compose entry point.
- `iosApp/` — the Swift-side wrapper around that entry point. **No `.xcodeproj` is checked in**
  — see `iosApp/README.md` for why and how to generate one; Android is fully runnable today,
  iOS needs that one-time step on a Mac.

## Running the Android app

1. Start the API (`../api`, `docker compose up -d && ./gradlew bootRun`).
2. Open this `app/` directory in Android Studio (Koala+ with the Kotlin Multiplatform plugin).
3. Run the `composeApp` configuration on an emulator — it points at `http://10.0.2.2:8080`,
   the emulator's alias for your host machine, so the local API "just works". Update
   `ApiConfig.BASE_URL` (`commonMain/network/HttpClientConfig.kt`) once you have a real deployed
   API URL.

## What's stubbed vs. real

Real and working:
- Shared data models, navigation, and screens (login → collection list → add/edit model)
- The Ktor API client, wired to every `/auth`, `/models`, `/automakers`, `/brands`,
  `/series` endpoint on the API
- Android token storage, and a real camera-capture flow using `ActivityResultContracts.TakePicture`
  + `FileProvider`

Stubbed with a `TODO` and a comment explaining the real implementation, because they need
either API credentials you haven't created yet or an Xcode project this container can't produce:
- Google Sign-In (Android: Credential Manager API; iOS: GoogleSignIn-iOS SDK)
- Sign in with Apple (iOS: `AuthenticationServices`; there's no first-party Apple SDK on Android
  — Apple's own guidance there is a web-based redirect flow)
- iOS camera capture (`UIImagePickerController`)
- Secure token storage (currently plain `SharedPreferences`/`NSUserDefaults` — fine for
  developing against, not for shipping)

None of these block building/running the app today — signing in with Google/Apple and taking a
photo on iOS will visibly "fail" (return to the login screen / no photo) until wired up, and
Android camera capture already works end-to-end.

## Model form fields

The add/edit screen intentionally exposes the brand-agnostic fields added on the API side:
packaging and condition (two separate pickers), series name/number (a brand's own wave or card number), a generic "rare variant"
checkbox in place of any single brand's own naming (Treasure Hunt, Super, Premium, ...), and free
text for notes — so cataloging a Hot Wheels, Matchbox, or California Collectibles piece uses the
same form without brand-specific fields bolted on.
