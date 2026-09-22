package com.diecastcollector.app.auth

/** Platform-specific context needed to construct platform services (e.g. Android's Context). */
expect class PlatformContext

/** Persists the app's own JWT across launches. */
expect class TokenStorage(context: PlatformContext) {
    fun save(token: String)
    fun load(): String?
    fun clear()
}
