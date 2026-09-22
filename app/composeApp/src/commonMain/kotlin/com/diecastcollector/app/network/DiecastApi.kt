package com.diecastcollector.app.network

import com.diecastcollector.app.model.Automaker
import com.diecastcollector.app.model.Brand
import com.diecastcollector.app.model.Collection
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.ModelRequest
import com.diecastcollector.app.model.PhotoUploadResponse
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
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

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

    suspend fun login(request: SocialLoginRequest): AuthResponse =
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

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

    suspend fun getCollections(): List<Collection> =
        client.get("$baseUrl/collections") { authorized() }.body()
}
