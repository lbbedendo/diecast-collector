package com.diecastcollector.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Automaker(val id: Long, val name: String)

@Serializable
data class Brand(val id: Long, val name: String)

@Serializable
data class Series(val id: Long, val name: String, val year: Int?, val brand: Brand?)

/** Whether a Model is still in its original packaging (see CONTEXT.md). */
@Serializable
enum class Packaging(val label: String) {
    SEALED("Sealed"),
    OPENED("Opened (packaging kept)"),
    LOOSE("Loose")
}

/** The physical state of the diecast itself, independent of its [Packaging]. */
@Serializable
enum class Condition(val label: String) {
    MINT("Mint"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor")
}

@Serializable
data class DiecastModel(
    val id: Long,
    val name: String,
    val automaker: Automaker?,
    val series: Series?,
    val scale: String?,
    val packaging: Packaging?,
    val condition: Condition?,
    val yearReleased: Int?,
    val color: String?,
    val notes: String?,
    val photoUrl: String?
)

@Serializable
data class ModelRequest(
    val name: String,
    val automakerId: Long?,
    val seriesId: Long?,
    val scale: String?,
    val packaging: Packaging?,
    val condition: Condition?,
    val yearReleased: Int?,
    val color: String?,
    val notes: String?
)

@Serializable
data class SocialLoginRequest(val provider: String, val idToken: String)

@Serializable
data class AuthResponse(val accessToken: String, val user: UserResponse)

@Serializable
data class UserResponse(val id: Long, val email: String, val displayName: String?)

@Serializable
data class PhotoUploadResponse(val photoUrl: String)
