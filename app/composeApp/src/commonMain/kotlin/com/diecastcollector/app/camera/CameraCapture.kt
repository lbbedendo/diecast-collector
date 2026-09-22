package com.diecastcollector.app.camera

/** A captured photo, ready to be uploaded. */
expect class CapturedPhoto {
    val fileName: String
    fun readBytes(): ByteArray
}

/** Launches the platform's native camera capture flow. */
expect class CameraLauncher {
    suspend fun capture(): CapturedPhoto?
}
