package com.diecastcollector.app.model

import com.diecastcollector.app.network.apiJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ToRequestTest {

    // Shape of GET /models/{id} for a Model with fields the app's form doesn't show.
    private val model = apiJson.decodeFromString(
        DiecastModel.serializer(),
        """{"id": 1, "name": "F2004", "vehicleYear": 2004, "scale": "SCALE_1_64", "color": "Red",
           "packaging": "SEALED", "condition": "MINT", "seriesNumber": "10/10", "chase": true,
           "purchasePrice": 12.90, "purchaseDate": "2026-03-14", "purchasedFrom": "Toy store",
           "notes": "First F1 car", "photoUrl": null,
           "automaker": {"id": 3, "name": "Ferrari"},
           "series": {"id": 5, "name": "HW Starting Grid", "year": 2026, "brand": {"id": 7, "name": "Hot Wheels"}}}"""
    )

    @Test
    fun savesTheModelUnchanged() {
        assertEquals(
            ModelRequest(
                name = "F2004",
                automakerId = 3,
                seriesId = 5,
                scale = Scale.SCALE_1_64,
                packaging = Packaging.SEALED,
                condition = Condition.MINT,
                chase = true,
                vehicleYear = 2004,
                color = "Red",
                notes = "First F1 car",
                seriesNumber = "10/10",
                purchasePrice = 12.90,
                purchaseDate = "2026-03-14",
                purchasedFrom = "Toy store"
            ),
            model.toRequest()
        )
    }

    @Test
    fun editSendsBackFieldsTheFormDoesNotShow() {
        // PUT /models/{id} replaces the whole Model: a field left out of the request is wiped.
        val edited = model.toRequest().copy(name = "F2004 (Schumacher)")
        val json = apiJson.encodeToJsonElement(ModelRequest.serializer(), edited).jsonObject

        assertEquals("10/10", json.getValue("seriesNumber").jsonPrimitive.content)
        assertEquals(12.90, json.getValue("purchasePrice").jsonPrimitive.double)
        assertEquals("2026-03-14", json.getValue("purchaseDate").jsonPrimitive.content)
        assertEquals("Toy store", json.getValue("purchasedFrom").jsonPrimitive.content)
    }
}
