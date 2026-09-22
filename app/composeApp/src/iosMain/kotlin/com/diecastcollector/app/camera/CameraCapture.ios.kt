package com.diecastcollector.app.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData

/**
 * Holds the captured JPEG data in memory. iOS has no need for a temp file the way the
 * Android implementation does, since `UIImagePickerController` hands back the image directly.
 */
actual class CapturedPhoto(private val data: NSData, actual val fileName: String) {
    @OptIn(ExperimentalForeignApi::class)
    actual fun readBytes(): ByteArray {
        val bytes = ByteArray(data.length.toInt())
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return bytes
    }
}

/**
 * Wraps `UIImagePickerController` with `sourceType = .camera`.
 *
 * TODO: present a `UIImagePickerController` from the current `UIViewController`, and in its
 * delegate's `imagePickerController(_:didFinishPickingMediaWithInfo:)` convert the picked
 * `UIImage` to JPEG `NSData` via `UIImageJPEGRepresentation` and resolve it as a [CapturedPhoto].
 */
actual class CameraLauncher {
    actual suspend fun capture(): CapturedPhoto? = suspendCancellableCoroutine { continuation ->
        continuation.resume(null) { _, _, _ -> }
    }
}
