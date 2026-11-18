package com.diecastcollector.app.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PhotoUrlsTest {

    @Test
    fun pathFromTheApiIsLoadedFromTheApi() {
        assertEquals("http://10.0.2.2:8080/photos/a.jpg", photoAddress("/photos/a.jpg", "http://10.0.2.2:8080"))
        assertEquals("https://api.example/photos/a.jpg", photoAddress("/photos/a.jpg", "https://api.example/"))
    }

    @Test
    fun absoluteUrlIsKept() {
        // In case photo storage moves to a bucket that hands out full URLs.
        assertEquals("https://cdn.example/a.jpg", photoAddress("https://cdn.example/a.jpg"))
    }

    @Test
    fun noPhoto() {
        assertNull(photoAddress(null))
        assertNull(photoAddress(""))
    }
}
