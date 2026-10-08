package com.diecastcollector.app.model

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The app and the API are separate Gradle projects, so the app can't reference the API's
 * `ModelScale` directly. Read its source from the repo instead: if the two enums drift, the API
 * rejects app requests with a 400 (or the app fails to decode a Model it doesn't know).
 */
class ScaleParityTest {

    // Android unit tests run with the module directory (app/composeApp) as the working directory.
    private val apiEnum = File("../../api/src/main/java/com/diecastcollector/api/enums/ModelScale.java")

    @Test
    fun appScaleMatchesApiModelScale() {
        val source = apiEnum.readText()
        val apiValues = Regex("""^\s+([A-Z0-9_]+)\("([^"]+)"\)""", RegexOption.MULTILINE)
            .findAll(source)
            .associate { it.groupValues[1] to it.groupValues[2] }

        assertEquals(apiValues, Scale.entries.associate { it.name to it.label })
    }
}
