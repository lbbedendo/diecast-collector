package com.diecastcollector.app.auth

import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Wraps Google Sign-In on iOS.
 *
 * TODO: wire up `GIDSignIn.sharedInstance.signInWithPresentingViewController(...)` from the
 * GoogleSignIn CocoaPod/SPM package once added to the Xcode project, resolving the ID token
 * from the returned `GIDGoogleUser`.
 */
actual class GoogleSignInLauncher {
    actual suspend fun signIn(): SocialSignInResult? = suspendCancellableCoroutine { continuation ->
        continuation.resume(null) { _, _, _ -> }
    }
}

/**
 * Wraps native "Sign in with Apple" via `ASAuthorizationAppleIDProvider`.
 *
 * TODO: perform the `ASAuthorizationController` request/delegate dance and resolve the
 * resulting `identityToken` (Data) as a UTF-8 string into a SocialSignInResult("apple", ...).
 */
actual class AppleSignInLauncher {
    actual suspend fun signIn(): SocialSignInResult? = suspendCancellableCoroutine { continuation ->
        continuation.resume(null) { _, _, _ -> }
    }
}
