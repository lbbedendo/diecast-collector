package com.diecastcollector.app.camera

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File

actual class CapturedPhoto(private val file: File) {
    actual val fileName: String get() = file.name
    actual fun readBytes(): ByteArray = file.readBytes()
}

/**
 * Launches the system camera app via [ActivityResultContracts.TakePicture], writing the
 * full-resolution photo to a cache file exposed through [FileProvider].
 *
 * Must be constructed while the activity is in the CREATED state (e.g. in `onCreate`,
 * before `onStart`) since [ComponentActivity.registerForActivityResult] requires it.
 */
actual class CameraLauncher(private val activity: ComponentActivity) {

    private var pendingFile: File? = null
    private var pendingContinuation: ((CapturedPhoto?) -> Unit)? = null

    private val launcher = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingFile
        pendingFile = null
        val result = if (success && file != null) CapturedPhoto(file) else null
        pendingContinuation?.invoke(result)
        pendingContinuation = null
    }

    actual suspend fun capture(): CapturedPhoto? = suspendCancellableCoroutine { continuation ->
        val cameraDir = File(activity.cacheDir, "camera").apply { mkdirs() }
        val file = File(cameraDir, "photo_${System.currentTimeMillis()}.jpg")
        pendingFile = file
        pendingContinuation = { result -> continuation.resume(result) { _, _, _ -> } }

        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )
        launcher.launch(uri)
    }
}
