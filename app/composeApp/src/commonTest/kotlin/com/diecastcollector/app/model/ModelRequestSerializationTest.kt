package com.diecastcollector.app.model

import com.diecastcollector.app.network.apiJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ModelRequestSerializationTest {

    private fun request(chase: Boolean? = null) = ModelRequest(
        name = "Ferrari F2004",
        automakerId = null,
        seriesId = null,
        scale = Scale.SCALE_1_64,
        packaging = Packaging.SEALED,
        condition = Condition.MINT,
        vehicleYear = 2004,
        color = null,
        notes = null
    ).let { if (chase != null) it.copy(chase = chase) else it }

    @Test
    fun chaseDefaultsToFalseAndIsAlwaysSent() {
        // The API rejects a Model request without chase, so the default must be encoded too.
        val json = apiJson.encodeToJsonElement(ModelRequest.serializer(), request()).jsonObject

        assertFalse(json.getValue("chase").jsonPrimitive.boolean)
    }

    @Test
    fun chaseTrueIsSent() {
        val json = apiJson.encodeToJsonElement(ModelRequest.serializer(), request(chase = true)).jsonObject

        assertEquals(true, json.getValue("chase").jsonPrimitive.boolean)
    }

    @Test
    fun fieldNamesMatchTheApi() {
        val json = apiJson.encodeToJsonElement(ModelRequest.serializer(), request()).jsonObject

        // The API takes the enum name, not the "1:64" label the form shows.
        assertEquals("SCALE_1_64", json.getValue("scale").jsonPrimitive.content)
        assertEquals("SEALED", json.getValue("packaging").jsonPrimitive.content)
        assertEquals("MINT", json.getValue("condition").jsonPrimitive.content)
        assertEquals(2004, json.getValue("vehicleYear").jsonPrimitive.content.toInt())
    }

    @Test
    fun everyScaleEncodesAsItsName() {
        // The API's ModelScale accepts names only; ScaleParityTest checks the names match the API's.
        for (scale in Scale.entries) {
            val json = apiJson.encodeToJsonElement(ModelRequest.serializer(), request().copy(scale = scale)).jsonObject
            assertEquals(scale.name, json.getValue("scale").jsonPrimitive.content)
        }
    }

    @Test
    fun scaleReadsBackFromApiResponse() {
        // Shape of GET /models/{id}: the API returns the enum name, which the app shows as "1:64".
        val model = apiJson.decodeFromString(
            DiecastModel.serializer(),
            """{"id": 1, "name": "Ferrari F2004", "automaker": null, "series": null, "scale": "SCALE_1_64",
               "packaging": null, "condition": null, "chase": false, "vehicleYear": 2004, "color": null,
               "notes": null, "photoUrl": null}"""
        )

        assertEquals(Scale.SCALE_1_64, model.scale)
        assertEquals("1:64", model.scale?.label)
    }

    @Test
    fun seriesRequestMatchesTheApi() {
        val json = apiJson.encodeToJsonElement(
            SeriesRequest.serializer(), SeriesRequest(brandId = 7, name = "HW Starting Grid", year = null)
        ).jsonObject

        assertEquals(7, json.getValue("brandId").jsonPrimitive.content.toInt())
        assertEquals("HW Starting Grid", json.getValue("name").jsonPrimitive.content)
        // The year is optional on the API side; it's sent as an explicit null.
        assertEquals("null", json.getValue("year").toString())
    }
}
