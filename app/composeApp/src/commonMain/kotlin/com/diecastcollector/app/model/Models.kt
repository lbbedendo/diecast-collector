package com.diecastcollector.app.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class Automaker(val id: Long, val name: String)

@Serializable
data class Brand(val id: Long, val name: String)

@Serializable
data class Series(val id: Long, val name: String, val year: Int?, val brand: Brand?)

/**
 * Mirrors the API's `ModelScale` enum: the API accepts only these names, not labels like "1:64".
 * `ScaleParityTest` fails if the two lists drift apart.
 */
@Serializable
enum class Scale(val label: String) {
    SCALE_1_12("1:12"),
    SCALE_1_18("1:18"),
    SCALE_1_24("1:24"),
    SCALE_1_32("1:32"),
    SCALE_1_36("1:36"),
    SCALE_1_38("1:38"),
    SCALE_1_43("1:43"),
    SCALE_1_50("1:50"),
    SCALE_1_64("1:64"),
    SCALE_1_72("1:72"),
    SCALE_1_76("1:76"),
    SCALE_1_87("1:87"),
    OTHER("Other")
}

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
    val scale: Scale?,
    val packaging: Packaging?,
    val condition: Condition?,
    val chase: Boolean = false,
    val vehicleYear: Int?,
    val color: String?,
    val notes: String?,
    val photoUrl: String?
)

/**
 * How a Model is titled in lists: "Ferrari F2004". A Model's name never includes its Automaker
 * (see Name in CONTEXT.md), so the two are joined here; a Model with no Automaker (a fictional
 * vehicle like "Bone Shaker") shows its name alone.
 */
val DiecastModel.displayName: String
    get() = listOfNotNull(automaker?.name, name).joinToString(" ")

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ModelRequest(
    val name: String,
    val automakerId: Long?,
    val seriesId: Long?,
    val scale: Scale?,
    val packaging: Packaging?,
    val condition: Condition?,
    // The API requires chase on every request, but apiJson omits default values unless told
    // otherwise, so a plain `= false` would never be sent.
    @EncodeDefault val chase: Boolean = false,
    val vehicleYear: Int?,
    val color: String?,
    val notes: String?
)

@Serializable
data class SeriesRequest(val brandId: Long, val name: String, val year: Int?)

@Serializable
data class SocialLoginRequest(val provider: String, val idToken: String)

@Serializable
data class AuthResponse(val accessToken: String, val user: UserResponse)

@Serializable
data class UserResponse(val id: Long, val email: String, val displayName: String?)

@Serializable
data class PhotoUploadResponse(val photoUrl: String)
