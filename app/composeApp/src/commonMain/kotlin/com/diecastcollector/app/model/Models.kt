package com.diecastcollector.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Automaker(val id: Long, val name: String)

@Serializable
data class Brand(val id: Long, val name: String)

@Serializable
data class Series(val id: Long, val name: String, val year: Int?, val brand: Brand?)

@Serializable
data class DiecastModel(
    val id: Long,
    val name: String,
    val automaker: Automaker?,
    val series: Series?,
    val scale: String?,
    val condition: String?,
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
    val condition: String?,
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
