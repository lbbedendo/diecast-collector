package com.diecastcollector.app

import androidx.compose.ui.window.ComposeUIViewController
import com.diecastcollector.app.auth.PlatformContext
import com.diecastcollector.app.auth.TokenStorage

fun MainViewController() = ComposeUIViewController {
    App(tokenStorage = TokenStorage(PlatformContext()))
}
