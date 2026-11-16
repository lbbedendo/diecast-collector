package com.diecastcollector.app.ui

import com.diecastcollector.app.model.Automaker
import com.diecastcollector.app.model.Brand
import com.diecastcollector.app.model.DiecastModel
import com.diecastcollector.app.model.Series
import com.diecastcollector.app.model.displayName
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelSearchTest {

    private val hotWheels = Brand(id = 1, name = "Hot Wheels")
    private val matchbox = Brand(id = 2, name = "Matchbox")

    private fun model(name: String, automaker: String?, series: Series? = null) = DiecastModel(
        id = 1,
        name = name,
        automaker = automaker?.let { Automaker(id = 1, name = it) },
        series = series,
        scale = null,
        packaging = null,
        condition = null,
        vehicleYear = null,
        color = null,
        notes = null,
        photoUrl = null
    )

    private val collection = listOf(
        model("2CV", "Citroën"),
        model("F2004", "Ferrari", Series(id = 1, name = "HW Starting Grid", year = 2026, brand = hotWheels)),
        model("Sauber C45", null, Series(id = 1, name = "HW Starting Grid", year = 2026, brand = hotWheels)),
        model("Land Cruiser", "Toyota", Series(id = 2, name = "Moving Parts", year = 2023, brand = matchbox))
    )

    private fun names(query: String) = searchModels(collection, query).map { it.displayName }

    @Test
    fun blankQueryKeepsEverything() {
        assertEquals(collection, searchModels(collection, "  "))
    }

    @Test
    fun matchesAutomakerAndNameIgnoringCaseAndAccents() {
        assertEquals(listOf("Citroën 2CV"), names("citroen"))
        assertEquals(listOf("Ferrari F2004"), names("ferrari f20"))
        assertEquals(listOf("Ferrari F2004"), names("F2004"))
    }

    @Test
    fun matchesSeriesName() {
        assertEquals(listOf("Ferrari F2004", "Sauber C45"), names("starting grid"))
    }

    @Test
    fun matchesBrand() {
        assertEquals(listOf("Toyota Land Cruiser"), names("matchbox"))
    }

    @Test
    fun noMatchIsEmpty() {
        assertEquals(emptyList(), names("lamborghini"))
    }
}
