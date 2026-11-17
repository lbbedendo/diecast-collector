package com.diecastcollector.app.photo

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleSizeTest {

    @Test
    fun smallPhotoIsDecodedWhole() {
        assertEquals(1, sampleSizeFor(1200, 900))
        assertEquals(1, sampleSizeFor(3199, 2000))
    }

    @Test
    fun bigPhotoIsDecodedNoSmallerThanTheTarget() {
        // 12MP camera photo: 4000/2 = 2000 is still at least 1600, 4000/4 = 1000 isn't.
        assertEquals(2, sampleSizeFor(4000, 3000))
        assertEquals(2, sampleSizeFor(3000, 4000))
        assertEquals(4, sampleSizeFor(8000, 6000))
    }
}
