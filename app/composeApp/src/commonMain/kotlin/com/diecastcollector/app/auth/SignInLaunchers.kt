package com.diecastcollector.app.auth

/** Result of a successful social sign-in: the raw ID token to send to the backend. */
data class SocialSignInResult(val provider: String, val idToken: String)

/** Launches the platform's native Google sign-in flow. */
expect class GoogleSignInLauncher {
    suspend fun signIn(): SocialSignInResult?
}

/** Launches the platform's native Apple sign-in flow. */
expect class AppleSignInLauncher {
    suspend fun signIn(): SocialSignInResult?
}
