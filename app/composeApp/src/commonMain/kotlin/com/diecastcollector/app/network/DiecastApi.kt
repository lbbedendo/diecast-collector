package com.diecastcollector.app.network

import com.diecastcollector.app.model.Automaker
import com.diecastcollector.app.model.Brand
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.ModelRequest
import com.diecastcollector.app.model.PhotoUploadResponse
import com.diecastcollector.app.model.Series
import com.diecastcollector.app.model.SeriesRequest
import com.diecastcollector.app.model.SocialLoginRequest
import com.diecastcollector.app.model.AuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Talks to the diecast-collector-api backend.
 *
 * [authTokenProvider] is read fresh on every request so a refreshed or cleared token
 * is always picked up, without any shared mutable global state.
 */
class DiecastApi(
    private val client: HttpClient,
    private val baseUrl: String = API_BASE_URL,
    private val authTokenProvider: () -> String?
) {
    private fun HttpRequestBuilder.authorized() {
        authTokenProvider()?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun login(request: SocialLoginRequest): AuthResponse {
        // The backend has one endpoint per provider (see AuthController), not a unified
        // /auth/login — route by the provider string set on SocialSignInResult ("google"/"apple").
        val endpoint = when (request.provider) {
            "google" -> "google"
            "apple" -> "apple"
            else -> throw IllegalArgumentException("Unsupported auth provider: ${request.provider}")
        }
        return client.post("$baseUrl/auth/$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // TODO: remove once real Google/Apple sign-in is wired up. Only works against a local API
    // started with APP_AUTH_DEV_LOGIN_ENABLED=true — see DevAuthController on the API side.
    suspend fun devLogin(): AuthResponse = client.post("$baseUrl/auth/dev").body()

    suspend fun getModels(): List<DiecastModel> =
        client.get("$baseUrl/models") { authorized() }.body()

    suspend fun getModel(id: Long): DiecastModel =
        client.get("$baseUrl/models/$id") { authorized() }.body()

    suspend fun createModel(request: ModelRequest): DiecastModel =
        client.post("$baseUrl/models") {
            authorized()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateModel(id: Long, request: ModelRequest): DiecastModel =
        client.put("$baseUrl/models/$id") {
            authorized()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteModel(id: Long) {
        client.delete("$baseUrl/models/$id") { authorized() }
    }

    suspend fun uploadPhoto(modelId: Long, fileName: String, bytes: ByteArray): PhotoUploadResponse =
        client.submitFormWithBinaryData(
            url = "$baseUrl/models/$modelId/photo",
            formData = formData {
                append("file", bytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            }
        ) { authorized() }.body()

    suspend fun getAutomakers(): List<Automaker> =
        client.get("$baseUrl/automakers") { authorized() }.body()

    suspend fun getBrands(): List<Brand> =
        client.get("$baseUrl/brands") { authorized() }.body()

    suspend fun getSeries(): List<Series> =
        client.get("$baseUrl/series") { authorized() }.body()

    /** Throws [ApiException] with the API's message, e.g. a 409 for a duplicate Series. */
    suspend fun createSeries(request: SeriesRequest): Series {
        val response = client.post("$baseUrl/series") {
            authorized()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw ApiException(apiErrorMessage(response.status.value, response.bodyAsText()))
        }
        return response.body()
    }
}
