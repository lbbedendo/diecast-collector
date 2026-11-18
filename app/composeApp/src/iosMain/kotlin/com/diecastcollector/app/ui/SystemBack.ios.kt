package com.diecastcollector.app.ui

import androidx.compose.runtime.Composable

/** iOS has no system back button; screens go back with their own back arrow. */
@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
}
