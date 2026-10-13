package com.diecastcollector.app.network

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiErrorTest {

    @Test
    fun usesTheMessageOfAConflict() {
        // Shape of GlobalExceptionHandler's 409 for a duplicate Series.
        val body = """{"timestamp": "2026-10-08T20:00:00Z", "status": 409,
            "message": "Series 'HW Starting Grid' (2026) already exists for brand Hot Wheels"}"""

        assertEquals("Series 'HW Starting Grid' (2026) already exists for brand Hot Wheels", apiErrorMessage(409, body))
    }

    @Test
    fun joinsValidationErrors() {
        val body = """{"timestamp": "2026-10-08T20:00:00Z", "status": 400, "errors": ["name: must not be blank", "brandId: must not be null"]}"""

        assertEquals("name: must not be blank\nbrandId: must not be null", apiErrorMessage(400, body))
    }

    @Test
    fun fallsBackToTheStatusWhenTheBodyIsNotJson() {
        assertEquals("Request failed (HTTP 502)", apiErrorMessage(502, "<html>Bad Gateway</html>"))
    }
}
