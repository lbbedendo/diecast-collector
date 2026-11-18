package com.diecastcollector.app.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

/** Longest side of an uploaded photo, in pixels: plenty for a phone screen, a few hundred KB as JPEG. */
internal const val MAX_PHOTO_SIDE = 1600

private const val JPEG_QUALITY = 85

/**
 * Re-encodes a photo as a JPEG no larger than [MAX_PHOTO_SIDE] on its longest side. A camera photo
 * can be bigger than the API's 10MB upload limit, and it is usually stored sideways with an EXIF
 * tag saying how to turn it, so the rotation is applied here (re-encoding drops the EXIF tag).
 *
 * [open] is called more than once: a stream is read once for the size, once for the pixels and
 * once for the EXIF tag. Returns null when the file isn't an image Android can decode.
 */
internal fun shrinkPhoto(open: () -> InputStream): PickedPhoto? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    open().use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight) }
    val decoded = open().use { BitmapFactory.decodeStream(it, null, options) } ?: return null
    val rotation = open().use { ExifInterface(it).rotationDegrees }

    val scale = MAX_PHOTO_SIDE.toFloat() / max(decoded.width, decoded.height)
    val matrix = Matrix().apply {
        if (scale < 1f) postScale(scale, scale)
        postRotate(rotation.toFloat())
    }
    val shrunk = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    val bytes = ByteArrayOutputStream().use { out ->
        shrunk.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        out.toByteArray()
    }
    return PickedPhoto(fileName = "photo_${System.currentTimeMillis()}.jpg", bytes = bytes)
}

/**
 * The largest power of two that still decodes the image at [MAX_PHOTO_SIDE] or more, so a 12MP
 * photo isn't fully decoded into memory just to be scaled down.
 */
internal fun sampleSizeFor(width: Int, height: Int): Int {
    var sample = 1
    while (max(width, height) / (sample * 2) >= MAX_PHOTO_SIDE) sample *= 2
    return sample
}
