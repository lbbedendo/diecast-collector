package com.diecastcollector.app.auth

/** Result of a successful social sign-in: the raw ID token to send to the backend. */
data class SocialSignInResult(val provider: String, val idToken: String)

/**
 * A provider's sign-in flow failed before the app had an ID token to send. Cancelling is not a
 * failure: launchers return `null` for it.
 */
class SignInFailure(val kind: Kind, cause: Throwable? = null) : Exception("Sign-in failed: $kind", cause) {
    enum class Kind {
        /** The build has no OAuth client ID to ask the provider for. */
        NOT_CONFIGURED,
        /** The device has no account the provider can sign in with. */
        NO_ACCOUNT,
        FAILED
    }
}

/**
 * Launches the platform's native Google sign-in flow. Returns `null` when the Collector cancels
 * it; throws [SignInFailure] when it fails.
 */
expect class GoogleSignInLauncher {
    suspend fun signIn(): SocialSignInResult?
}

/** Launches the platform's native Apple sign-in flow. */
expect class AppleSignInLauncher {
    suspend fun signIn(): SocialSignInResult?
}
