package com.diecastcollector.app.photo

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * The camera is the system camera app ([ActivityResultContracts.TakePicture]), writing to a cache
 * file shared through [FileProvider]. The gallery is the system Photo Picker
 * ([ActivityResultContracts.PickVisualMedia]), which needs no storage permission.
 *
 * Must be constructed while the activity is in the CREATED state (e.g. in `onCreate`, before
 * `onStart`), since [ComponentActivity.registerForActivityResult] requires it.
 */
actual class PhotoPicker(private val activity: ComponentActivity) {

    private var onCameraResult: ((Boolean) -> Unit)? = null
    private var onGalleryResult: ((Uri?) -> Unit)? = null

    private val camera = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        onCameraResult?.invoke(saved)
        onCameraResult = null
    }

    private val gallery = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onGalleryResult?.invoke(uri)
        onGalleryResult = null
    }

    actual suspend fun takePhoto(): PickedPhoto? {
        val dir = File(activity.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
        return try {
            val saved = suspendCancellableCoroutine { continuation ->
                onCameraResult = { continuation.resume(it) }
                camera.launch(uri)
            }
            if (saved) withContext(Dispatchers.IO) { shrinkPhoto { file.inputStream() } } else null
        } finally {
            file.delete()
        }
    }

    actual suspend fun pickFromGallery(): PickedPhoto? {
        val uri = suspendCancellableCoroutine<Uri?> { continuation ->
            onGalleryResult = { continuation.resume(it) }
            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } ?: return null
        return withContext(Dispatchers.IO) {
            shrinkPhoto { activity.contentResolver.openInputStream(uri) ?: error("Can't open $uri") }
        }
    }
}
