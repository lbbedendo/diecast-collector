package com.diecastcollector.app.network

/**
 * The address to load a Model's photo from. The API gives the photo's path ("/photos/<file>"), and
 * serves it without a token, so the image loader only needs it made absolute.
 */
fun photoAddress(photoUrl: String?, baseUrl: String = API_BASE_URL): String? {
    if (photoUrl.isNullOrBlank()) return null
    if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) return photoUrl
    return baseUrl.trimEnd('/') + "/" + photoUrl.trimStart('/')
}
