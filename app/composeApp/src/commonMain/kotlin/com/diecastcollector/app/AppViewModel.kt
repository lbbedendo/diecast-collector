package com.diecastcollector.app

import com.diecastcollector.app.auth.TokenStorage
import com.diecastcollector.app.model.Automaker
import com.diecastcollector.app.model.Brand
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.ModelRequest
import com.diecastcollector.app.model.Series
import com.diecastcollector.app.model.SeriesRequest
import com.diecastcollector.app.model.SocialLoginRequest
import com.diecastcollector.app.network.DiecastApi
import com.diecastcollector.app.network.createHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val models: List<DiecastModel> = emptyList(),
    val automakers: List<Automaker> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val series: List<Series> = emptyList(),
    val error: String? = null
)

class AppViewModel(private val tokenStorage: TokenStorage) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val api = DiecastApi(
        client = createHttpClient(),
        authTokenProvider = { tokenStorage.load() }
    )

    init {
        _uiState.value = _uiState.value.copy(isLoggedIn = tokenStorage.load() != null)
    }

    suspend fun login(provider: String, idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching {
            api.login(SocialLoginRequest(provider = provider, idToken = idToken))
        }.onSuccess { response ->
            tokenStorage.save(response.accessToken)
            _uiState.value = _uiState.value.copy(isLoggedIn = true, isLoading = false)
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
        }
    }

    // TODO: remove once real Google/Apple sign-in is wired up — see DiecastApi.devLogin().
    suspend fun devLogin() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching { api.devLogin() }
            .onSuccess { response ->
                tokenStorage.save(response.accessToken)
                _uiState.value = _uiState.value.copy(isLoggedIn = true, isLoading = false)
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
            }
    }

    fun logout() {
        tokenStorage.clear()
        _uiState.value = AppUiState(isLoggedIn = false)
    }

    suspend fun loadModels() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching { api.getModels() }
            .onSuccess { models -> _uiState.value = _uiState.value.copy(models = models, isLoading = false) }
            .onFailure { error -> _uiState.value = _uiState.value.copy(isLoading = false, error = error.message) }
    }

    suspend fun loadLookups() {
        runCatching {
            Triple(api.getAutomakers(), api.getBrands(), api.getSeries())
        }.onSuccess { (automakers, brands, series) ->
            _uiState.value = _uiState.value.copy(automakers = automakers, brands = brands, series = series)
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(error = error.message)
        }
    }

    /**
     * Creates a Series from the Model form and adds it to the picker's options. The failure (e.g.
     * a duplicate Series) is returned for the dialog to show, not put in the screen-wide error.
     */
    suspend fun createSeries(request: SeriesRequest): Result<Series> =
        runCatching { api.createSeries(request) }
            .onSuccess { created ->
                _uiState.value = _uiState.value.copy(series = (_uiState.value.series + created).sortedBy { it.name })
            }

    suspend fun createModel(request: ModelRequest): DiecastModel? =
        runCatching { api.createModel(request) }
            .onSuccess { loadModels() }
            .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
            .getOrNull()

    suspend fun uploadPhoto(modelId: Long, fileName: String, bytes: ByteArray) {
        runCatching { api.uploadPhoto(modelId, fileName, bytes) }
            .onSuccess { loadModels() }
            .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
    }

    suspend fun deleteModel(id: Long) {
        runCatching { api.deleteModel(id) }
            .onSuccess { loadModels() }
            .onFailure { error -> _uiState.value = _uiState.value.copy(error = error.message) }
    }
}
