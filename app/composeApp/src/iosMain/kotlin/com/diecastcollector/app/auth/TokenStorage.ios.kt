package com.diecastcollector.app.auth

import platform.Foundation.NSUserDefaults

actual class PlatformContext

actual class TokenStorage actual constructor(context: PlatformContext) {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun save(token: String) {
        defaults.setObject(token, forKey = KEY_TOKEN)
    }

    actual fun load(): String? = defaults.stringForKey(KEY_TOKEN)

    actual fun clear() {
        defaults.removeObjectForKey(KEY_TOKEN)
    }

    private companion object {
        const val KEY_TOKEN = "token"
    }
}
