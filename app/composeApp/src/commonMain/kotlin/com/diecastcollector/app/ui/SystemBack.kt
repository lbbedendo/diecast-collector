package com.diecastcollector.app.ui

import androidx.compose.runtime.Composable

/**
 * Runs [onBack] on the platform's system back (Android's back button or gesture) while [enabled],
 * instead of the default of leaving the app. While disabled, back does the default.
 */
@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
