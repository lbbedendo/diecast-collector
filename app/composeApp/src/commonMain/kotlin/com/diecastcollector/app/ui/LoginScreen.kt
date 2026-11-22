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
    onGoogleLogin: () -> Unit,
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

        Button(onClick = onGoogleLogin, enabled = !uiState.isLoading, modifier = Modifier.padding(top = 24.dp)) {
            Text("Entrar com o Google")
        }
        // TODO: wire to AppleSignInLauncher, like the Google button.
        Button(onClick = { }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Entrar com a Apple")
        }

        // Only works against a local API started with APP_AUTH_DEV_LOGIN_ENABLED=true; a real
        // deployment has no /auth/dev route to call, so this is harmless to leave visible.
        OutlinedButton(onClick = onDevLogin, modifier = Modifier.padding(top = 24.dp)) {
            Text("Login de desenvolvimento (somente local)")
        }
    }
}
