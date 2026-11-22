package com.diecastcollector.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diecastcollector.app.auth.GoogleSignInLauncher
import com.diecastcollector.app.auth.PlatformContext
import com.diecastcollector.app.auth.TokenStorage
import com.diecastcollector.app.photo.PhotoPicker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenStorage = TokenStorage(PlatformContext(applicationContext))
        // Registers for activity results, which has to happen here, before the activity starts.
        val photoPicker = PhotoPicker(this)
        val googleSignIn = GoogleSignInLauncher(this, BuildConfig.GOOGLE_SERVER_CLIENT_ID)

        setContent {
            App(tokenStorage = tokenStorage, photoPicker = photoPicker, googleSignIn = googleSignIn)
        }
    }
}
