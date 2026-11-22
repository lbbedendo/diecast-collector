package com.diecastcollector.app.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CancelledByUserTest {

    @Test
    fun backingOutOfTheSheetIsACancel() {
        assertTrue(isCancelledByUser("[16] Cancelled by user."))
    }

    @Test
    fun googleFailuresReportedAsCancelsAreNot() {
        // What Credential Manager returns when the app has no Android OAuth client in the Cloud
        // project (status UNREGISTERED_ON_API_CONSOLE): silent, it looked like nothing happened.
        assertFalse(isCancelledByUser("[16] Account reauth failed."))
        assertFalse(isCancelledByUser(null))
    }
}
