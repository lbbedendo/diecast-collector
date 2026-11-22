# Diecast Collector — Mobile App

Kotlin Multiplatform + Compose Multiplatform app: one shared UI codebase targeting Android and
iOS.

## Layout

- `composeApp/src/commonMain` — shared UI (Compose), data models, the Ktor API client, and
  `expect` declarations for the few things that must be platform-native (secure token storage,
  photo picking from the camera or gallery, Google/Apple sign-in launchers).
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
- Android token storage
- Google Sign-In on Android, through Credential Manager (`GetSignInWithGoogleOption`): the ID
  token goes to `POST /auth/google`. The build needs the Web OAuth client ID Google issues that
  token for — the API's `GOOGLE_CLIENT_ID` — as `google.serverClientId` in `app/local.properties`
  (gitignored) or the `GOOGLE_CLIENT_ID` environment variable. Without it the button says sign-in
  isn't configured. Google also needs an Android OAuth client in the same Cloud project with the
  app's package name and the signing key's SHA-1, and the device a Google account
- Model photos: taken with the camera (`ActivityResultContracts.TakePicture` + `FileProvider`) or
  picked from the gallery (the system Photo Picker, no storage permission), shrunk to 1600px and
  turned upright (EXIF) on the device, uploaded after the Model is saved, and shown with Coil 3 as
  a thumbnail in the list and at the top of the form

Stubbed with a `TODO` and a comment explaining the real implementation, because they need
either API credentials you haven't created yet or an Xcode project this container can't produce:
- Google Sign-In on iOS (GoogleSignIn-iOS SDK)
- Sign in with Apple (iOS: `AuthenticationServices`; there's no first-party Apple SDK on Android
  — Apple's own guidance there is a web-based redirect flow)
- iOS photo picker (`UIImagePickerController` for the camera, `PHPickerViewController` for the gallery)
- Secure token storage (currently plain `SharedPreferences`/`NSUserDefaults` — fine for
  developing against, not for shipping)

None of these block building/running the app today — signing in with Apple, or with Google on
iOS, and taking a photo on iOS will visibly "fail" (stay on the login screen / no photo) until
wired up; Google sign-in and photos already work end-to-end on Android.

## Model form fields

The add screen uses only brand-agnostic fields, so a Hot Wheels, Matchbox, or California
Collectibles piece goes through the same form: automaker (type to filter the list; accents are
ignored, so "skoda" finds Škoda), name (without the automaker), vehicle year (the real car's,
optional), Series (shown as "Brand · Name (year)", with a "+" to create one in a dialog without
leaving the form), scale, packaging and condition (two separate pickers), a Chase checkbox (off
by default), color, and notes. The API also accepts a Series number and purchase details, but the
form doesn't expose them yet.
