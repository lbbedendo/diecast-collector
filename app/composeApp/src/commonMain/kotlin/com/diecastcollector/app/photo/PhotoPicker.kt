package com.diecastcollector.app.photo

/** A photo picked for a Model, ready to upload: JPEG bytes already shrunk to fit the API's upload limit. */
class PickedPhoto(val fileName: String, val bytes: ByteArray)

/**
 * Gets a photo for a Model from the platform's camera or gallery. Both return null when the
 * Collector backs out without a photo.
 */
expect class PhotoPicker {
    suspend fun takePhoto(): PickedPhoto?
    suspend fun pickFromGallery(): PickedPhoto?
}
