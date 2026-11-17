package com.diecastcollector.app

import androidx.compose.ui.window.ComposeUIViewController
import com.diecastcollector.app.auth.PlatformContext
import com.diecastcollector.app.auth.TokenStorage
import com.diecastcollector.app.photo.PhotoPicker

fun MainViewController() = ComposeUIViewController {
    App(tokenStorage = TokenStorage(PlatformContext()), photoPicker = PhotoPicker())
}
