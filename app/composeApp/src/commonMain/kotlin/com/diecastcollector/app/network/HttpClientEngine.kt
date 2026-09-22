package com.diecastcollector.app.network

import io.ktor.client.engine.HttpClientEngineFactory

/** Provides the platform-appropriate Ktor engine (OkHttp on Android, Darwin on iOS). */
expect fun httpClientEngineFactory(): HttpClientEngineFactory<*>
