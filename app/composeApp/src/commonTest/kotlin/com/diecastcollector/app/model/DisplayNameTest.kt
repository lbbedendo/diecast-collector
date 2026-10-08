package com.diecastcollector.app.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayNameTest {

    private fun model(name: String, automaker: String?) = DiecastModel(
        id = 1,
        name = name,
        automaker = automaker?.let { Automaker(id = 1, name = it) },
        series = null,
        scale = null,
        packaging = null,
        condition = null,
        vehicleYear = null,
        color = null,
        notes = null,
        photoUrl = null
    )

    @Test
    fun automakerPrefixesTheName() {
        assertEquals("Ferrari F2004", model("F2004", "Ferrari").displayName)
    }

    @Test
    fun nameAloneWithoutAutomaker() {
        // Fictional vehicles have no Automaker.
        assertEquals("Bone Shaker", model("Bone Shaker", null).displayName)
    }
}
