package com.diecastcollector.app.network

import kotlinx.serialization.Serializable

/**
 * A non-2xx API response. [message] is the API's own (English) text, kept for logs and debugging;
 * what the Collector sees is chosen from [status] by `errorMessage` in the ui package.
 */
class ApiException(val status: Int, message: String) : Exception(message)

/**
 * Error body shapes from the API's GlobalExceptionHandler: `{"message": ...}` for 404/409, or
 * `{"errors": ["field: problem", ...]}` for a 400 validation failure.
 */
@Serializable
private data class ApiErrorBody(val message: String? = null, val errors: List<String>? = null)

/** The API's message for an error response; falls back to the HTTP status. */
internal fun apiErrorMessage(status: Int, body: String): String {
    val parsed = runCatching { apiJson.decodeFromString(ApiErrorBody.serializer(), body) }.getOrNull()
    return parsed?.message?.takeIf { it.isNotBlank() }
        ?: parsed?.errors?.takeIf { it.isNotEmpty() }?.joinToString("\n")
        ?: "Request failed (HTTP $status)"
}
