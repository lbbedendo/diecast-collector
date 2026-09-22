package com.diecastcollector.app.auth

import android.content.Context
import android.content.SharedPreferences

actual typealias PlatformContext = Context

actual class TokenStorage actual constructor(context: PlatformContext) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("diecast_collector_auth", Context.MODE_PRIVATE)

    actual fun save(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    actual fun load(): String? = prefs.getString(KEY_TOKEN, null)

    actual fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val KEY_TOKEN = "token"
    }
}
