package com.diecastcollector.app

import com.diecastcollector.app.auth.GoogleSignInLauncher
import com.diecastcollector.app.auth.SocialSignInResult
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
import com.diecastcollector.app.photo.PickedPhoto
import com.diecastcollector.app.ui.PHOTO_NOT_SENT
import com.diecastcollector.app.ui.errorMessage
import com.diecastcollector.app.ui.signInErrorMessage
import com.diecastcollector.app.ui.sortedForList
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

    /**
     * Runs a provider's sign-in flow ([launch], e.g. [GoogleSignInLauncher.signIn]) and trades its
     * ID token for the API's own token. Cancelling the flow just stops, with no error.
     */
    suspend fun signIn(launch: suspend () -> SocialSignInResult?) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching {
            val signedIn = launch() ?: return@runCatching null
            api.login(SocialLoginRequest(provider = signedIn.provider, idToken = signedIn.idToken))
        }.onSuccess { response ->
            if (response != null) tokenStorage.save(response.accessToken)
            _uiState.value = _uiState.value.copy(isLoggedIn = response != null, isLoading = false)
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = signInErrorMessage(error))
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
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage(error))
            }
    }

    fun logout() {
        tokenStorage.clear()
        _uiState.value = AppUiState(isLoggedIn = false)
    }

    suspend fun loadModels() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching { api.getModels() }
            .onSuccess { models -> _uiState.value = _uiState.value.copy(models = sortedForList(models), isLoading = false) }
            .onFailure { error -> _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage(error)) }
    }

    suspend fun loadLookups() {
        runCatching {
            Triple(api.getAutomakers(), api.getBrands(), api.getSeries())
        }.onSuccess { (automakers, brands, series) ->
            _uiState.value = _uiState.value.copy(automakers = automakers, brands = brands, series = series)
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(error = errorMessage(error))
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

    suspend fun createModel(request: ModelRequest, photo: PickedPhoto? = null): DiecastModel? =
        saveModel(photo) { api.createModel(request) }

    suspend fun updateModel(id: Long, request: ModelRequest, photo: PickedPhoto? = null): DiecastModel? =
        saveModel(photo) { api.updateModel(id, request) }

    /**
     * Saves a Model, then uploads its new [photo], if any: the upload needs the saved Model's id,
     * which a new Model only has once created. A failed upload keeps the saved Model and says so.
     */
    private suspend fun saveModel(photo: PickedPhoto?, save: suspend () -> DiecastModel): DiecastModel? {
        val saved = runCatching { save() }.getOrElse { error ->
            _uiState.value = _uiState.value.copy(error = errorMessage(error))
            return null
        }
        val photoFailed = photo != null &&
            runCatching { api.uploadPhoto(saved.id, photo.fileName, photo.bytes) }.isFailure
        loadModels()
        // After loadModels(), which clears the error when it starts.
        if (photoFailed) _uiState.value = _uiState.value.copy(error = PHOTO_NOT_SENT)
        return saved
    }

    suspend fun deleteModel(id: Long) {
        runCatching { api.deleteModel(id) }
            .onSuccess { loadModels() }
            .onFailure { error -> _uiState.value = _uiState.value.copy(error = errorMessage(error)) }
    }
}
