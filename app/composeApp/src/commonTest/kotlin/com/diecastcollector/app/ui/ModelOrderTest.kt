package com.diecastcollector.app.ui

import com.diecastcollector.app.model.Automaker
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.displayName
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelOrderTest {

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
    fun sortsByTheNameTheListShows() {
        // "Bone Shaker" has no Automaker, so it sorts by its name alone.
        val models = listOf(
            model("F2004", "Ferrari"),
            model("Caravan", "Chevrolet"),
            model("Bone Shaker", null),
            model("911", "Porsche")
        )

        assertEquals(
            listOf("Bone Shaker", "Chevrolet Caravan", "Ferrari F2004", "Porsche 911"),
            sortedForList(models).map { it.displayName }
        )
    }

    @Test
    fun ignoresCaseAndAccents() {
        val models = listOf(model("Octavia", "Škoda"), model("model", "tesla"), model("Clio", "Renault"))

        assertEquals(
            listOf("Renault Clio", "Škoda Octavia", "tesla model"),
            sortedForList(models).map { it.displayName }
        )
    }
}
