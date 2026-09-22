package com.diecastcollector.app.auth

import androidx.activity.ComponentActivity
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Wraps Android's Credential Manager (Google) sign-in flow.
 *
 * TODO: wire up `androidx.credentials.CredentialManager` and
 * `com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption`
 * once the corresponding Gradle dependencies are added; this returns the ID
 * token from the resulting `GoogleIdTokenCredential`.
 */
actual class GoogleSignInLauncher(private val activity: ComponentActivity) {
    actual suspend fun signIn(): SocialSignInResult? = suspendCancellableCoroutine { continuation ->
        // TODO: launch CredentialManager.create(activity).getCredential(...) and resolve
        // the ID token into a SocialSignInResult("google", idToken).
        continuation.resume(null) { _, _, _ -> }
    }
}

/**
 * Wraps Apple's "Sign in with Apple" flow on Android, which goes through Apple's
 * web-based OAuth flow (there is no native Android SDK).
 *
 * TODO: launch a Custom Tab / WebView against Apple's authorize endpoint and
 * capture the resulting `id_token` via the redirect URI.
 */
actual class AppleSignInLauncher(private val activity: ComponentActivity) {
    actual suspend fun signIn(): SocialSignInResult? = suspendCancellableCoroutine { continuation ->
        continuation.resume(null) { _, _, _ -> }
    }
}
