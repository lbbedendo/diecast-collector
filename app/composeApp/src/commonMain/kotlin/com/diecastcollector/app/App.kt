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
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.diecastcollector.app.auth.TokenStorage
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.photo.PhotoPicker
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
fun App(tokenStorage: TokenStorage, photoPicker: PhotoPicker) {
    // Model photos load over Ktor, the HTTP client the app already uses for the API.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    val viewModel = remember { AppViewModel(tokenStorage) }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    // Kept here, not in the list screen, so the search survives a round trip to the Model form.
    var searchQuery by remember { mutableStateOf("") }
    var screen by remember { mutableStateOf<Screen>(if (uiState.isLoggedIn) Screen.List else Screen.Login) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            screen = Screen.List
            viewModel.loadModels()
            viewModel.loadLookups()
        } else {
            screen = Screen.Login
            searchQuery = ""
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
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onAddClick = { screen = Screen.NewModel },
                    onModelClick = { model -> screen = Screen.EditModel(model) },
                    onDelete = { id -> scope.launch { viewModel.deleteModel(id) } },
                    onLogout = { viewModel.logout() }
                )
                Screen.NewModel -> ModelEditScreen(
                    uiState = uiState,
                    onSave = { request, photo ->
                        scope.launch {
                            viewModel.createModel(request, photo)
                            screen = Screen.List
                        }
                    },
                    onTakePhoto = { photoPicker.takePhoto() },
                    onPickPhoto = { photoPicker.pickFromGallery() },
                    onCreateSeries = { request -> viewModel.createSeries(request) },
                    onCancel = { screen = Screen.List }
                )
                is Screen.EditModel -> ModelEditScreen(
                    uiState = uiState,
                    model = current.model,
                    onSave = { request, photo ->
                        scope.launch {
                            viewModel.updateModel(current.model.id, request, photo)
                            screen = Screen.List
                        }
                    },
                    onTakePhoto = { photoPicker.takePhoto() },
                    onPickPhoto = { photoPicker.pickFromGallery() },
                    onCreateSeries = { request -> viewModel.createSeries(request) },
                    onCancel = { screen = Screen.List }
                )
            }
        }
    }
}
