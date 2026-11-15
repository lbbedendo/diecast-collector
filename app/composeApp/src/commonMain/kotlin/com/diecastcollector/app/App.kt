package com.diecastcollector.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.diecastcollector.app.auth.TokenStorage
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.ui.CollectionListScreen
import com.diecastcollector.app.ui.LoginScreen
import com.diecastcollector.app.ui.ModelEditScreen
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Login : Screen
    data object List : Screen
    data object NewModel : Screen
    data class EditModel(val model: DiecastModel) : Screen
}

@Composable
fun App(tokenStorage: TokenStorage) {
    val viewModel = remember { AppViewModel(tokenStorage) }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf<Screen>(if (uiState.isLoggedIn) Screen.List else Screen.Login) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            screen = Screen.List
            viewModel.loadModels()
            viewModel.loadLookups()
        } else {
            screen = Screen.Login
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val current = screen) {
                Screen.Login -> LoginScreen(
                    uiState = uiState,
                    onLogin = { provider, idToken -> scope.launch { viewModel.login(provider, idToken) } },
                    onDevLogin = { scope.launch { viewModel.devLogin() } }
                )
                Screen.List -> CollectionListScreen(
                    uiState = uiState,
                    onAddClick = { screen = Screen.NewModel },
                    onModelClick = { model -> screen = Screen.EditModel(model) },
                    onDelete = { id -> scope.launch { viewModel.deleteModel(id) } },
                    onLogout = { viewModel.logout() }
                )
                Screen.NewModel -> ModelEditScreen(
                    uiState = uiState,
                    onSave = { request ->
                        scope.launch {
                            viewModel.createModel(request)
                            screen = Screen.List
                        }
                    },
                    onCreateSeries = { request -> viewModel.createSeries(request) },
                    onCancel = { screen = Screen.List }
                )
                is Screen.EditModel -> ModelEditScreen(
                    uiState = uiState,
                    model = current.model,
                    onSave = { request ->
                        scope.launch {
                            viewModel.updateModel(current.model.id, request)
                            screen = Screen.List
                        }
                    },
                    onCreateSeries = { request -> viewModel.createSeries(request) },
                    onCancel = { screen = Screen.List }
                )
            }
        }
    }
}
