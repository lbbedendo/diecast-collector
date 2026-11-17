package com.diecastcollector.app.photo

/**
 * TODO: take a photo with `UIImagePickerController` (`sourceType = .camera`) and pick one with
 * `PHPickerViewController`, both presented from the current `UIViewController`. Convert the
 * `UIImage` to JPEG with `UIImageJPEGRepresentation`, shrunk like the Android `shrinkPhoto`, and
 * copy the `NSData` bytes into a [PickedPhoto]. Needs an Xcode project to build and try.
 */
actual class PhotoPicker {
    actual suspend fun takePhoto(): PickedPhoto? = null
    actual suspend fun pickFromGallery(): PickedPhoto? = null
}
