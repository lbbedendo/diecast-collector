package com.diecastcollector.app.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** The JSON format used for every API call; shared with tests so they encode exactly as the app does. */
internal val apiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun createHttpClient(): HttpClient = HttpClient(httpClientEngineFactory()) {
    install(ContentNegotiation) {
        json(apiJson)
    }
    install(Logging) {
        level = LogLevel.INFO
    }
}
