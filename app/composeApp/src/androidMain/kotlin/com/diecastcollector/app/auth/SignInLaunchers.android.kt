package com.diecastcollector.app.auth

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Signs in with Google through Android's Credential Manager, which shows Google's account sheet
 * and returns an ID token issued for [serverClientId] — the Web OAuth client ID the API checks as
 * the token's audience (its `GOOGLE_CLIENT_ID`).
 */
actual class GoogleSignInLauncher(
    private val activity: ComponentActivity,
    private val serverClientId: String
) {
    actual suspend fun signIn(): SocialSignInResult? {
        if (serverClientId.isBlank()) throw SignInFailure(SignInFailure.Kind.NOT_CONFIGURED)

        // The "Sign in with Google" option is the one meant for a button tap: it lists every
        // Google account on the device and lets the Collector add one.
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId).build())
            .build()
        val credential = try {
            CredentialManager.create(activity).getCredential(activity, request).credential
        } catch (e: GetCredentialCancellationException) {
            return null
        } catch (e: NoCredentialException) {
            throw SignInFailure(SignInFailure.Kind.NO_ACCOUNT, e)
        } catch (e: GetCredentialException) {
            throw SignInFailure(SignInFailure.Kind.FAILED, e)
        }

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw SignInFailure(SignInFailure.Kind.FAILED)
        }
        val idToken = try {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (e: GoogleIdTokenParsingException) {
            throw SignInFailure(SignInFailure.Kind.FAILED, e)
        }
        return SocialSignInResult("google", idToken)
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
