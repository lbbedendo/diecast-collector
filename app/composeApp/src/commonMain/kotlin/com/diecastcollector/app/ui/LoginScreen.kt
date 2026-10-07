package com.diecastcollector.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diecastcollector.app.AppUiState

@Composable
fun LoginScreen(
    uiState: AppUiState,
    onLogin: (provider: String, idToken: String) -> Unit,
    // TODO: remove once real Google/Apple sign-in is wired up — see AppViewModel.devLogin().
    onDevLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Diecast Collector")

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        uiState.error?.let { error ->
            Text(error, modifier = Modifier.padding(top = 8.dp))
        }

        // The platform-specific sign-in launchers (GoogleSignInLauncher / AppleSignInLauncher)
        // are wired up by the app entry point on each platform, which owns the Activity/
        // presenting UIViewController they need; the resulting SocialSignInResult is passed
        // here via onLogin.
        Button(onClick = { /* Wired to GoogleSignInLauncher by the platform entry point */ }, modifier = Modifier.padding(top = 24.dp)) {
            Text("Sign in with Google")
        }
        Button(onClick = { /* Wired to AppleSignInLauncher by the platform entry point */ }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Sign in with Apple")
        }

        // Only works against a local API started with APP_AUTH_DEV_LOGIN_ENABLED=true; a real
        // deployment has no /auth/dev route to call, so this is harmless to leave visible.
        OutlinedButton(onClick = onDevLogin, modifier = Modifier.padding(top = 24.dp)) {
            Text("Dev login (local only)")
        }
    }
}
