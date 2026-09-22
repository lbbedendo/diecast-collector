# iosApp

This folder holds the Swift-side entry point for the iOS app — `iosApp.swift` and
`ContentView.swift`, which just wrap the shared Compose UI (`MainViewController()` in
`composeApp/src/iosMain`).

## What's *not* here on purpose

There's deliberately no `.xcodeproj`/`.xcworkspace` in this folder. That file format is a
plist-like project graph that Xcode itself writes and rewrites constantly (build settings,
signing, target membership, Swift Package references); hand-authoring one outside Xcode is a
common source of "works on my machine" project corruption, and this scaffold was built in a
Linux container with no Xcode to actually generate and verify one against. Generating it for
real needs a Mac with Xcode, and takes one of two paths:

1. **Recommended:** Open Android Studio (with the Kotlin Multiplatform plugin) on this project,
   and use *Tools → Kotlin Multiplatform → Open iOS project in Xcode* (or run the equivalent
   `pod install`/KMP wizard step) — it scaffolds an `iosApp.xcodeproj` wired to this module.
2. Start a fresh KMP project at [kmp.jetbrains.com](https://kmp.jetbrains.com) with the same
   package name (`com.diecastcollector.app`) and copy its generated `iosApp/*.xcodeproj` here,
   then drop in the two Swift files below in place of its defaults.

Once you have a real Xcode project, add these two files to it:

- `iosApp/iosApp.swift` — the `@main App` struct
- `iosApp/ContentView.swift` — wraps `MainViewController()` via `UIViewControllerRepresentable`

Both are provided in this folder ready to drop in.

## Why the camera/sign-in TODOs live in Xcode's court

`CameraCapture.ios.kt`, `SignInLaunchers.ios.kt` (Apple) and the Google Sign-In equivalent are
stubbed rather than fully implemented, because:
- Sign in with Apple (`AuthenticationServices`) and `UIImagePickerController` both need a
  delegate object tied to the current `UIViewController`/window scene, which is far easier to
  wire and debug interactively in Xcode/Simulator than to write blind here.
- Google Sign-In on iOS wants its SDK added as a Swift Package dependency, which only makes
  sense once there's a real `.xcodeproj` to add it to.

The shared Kotlin code compiles and the app runs on iOS as-is (login/camera buttons just report
"cancelled" until wired up) — this is meant to unblock Android development immediately while
leaving clearly-marked next steps for the iOS-specific SDK glue.
