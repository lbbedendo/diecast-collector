package com.diecastcollector.app.ui

import com.diecastcollector.app.network.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessagesTest {

    @Test
    fun conflictUsesTheCallersMessage() {
        val duplicate = ApiException(409, "Series 'HW Starting Grid' (2026) already exists for brand Hot Wheels")

        assertEquals(
            "Já existe uma série com essa marca, nome e ano.",
            errorMessage(duplicate, onConflict = "Já existe uma série com essa marca, nome e ano.")
        )
        assertEquals("Já existe um registro igual.", errorMessage(duplicate))
    }

    @Test
    fun apiStatusesMapToPortuguese() {
        assertEquals("Dados inválidos. Revise os campos e tente novamente.", errorMessage(ApiException(400, "name: must not be blank")))
        assertEquals("Sua sessão expirou. Entre novamente.", errorMessage(ApiException(401, "Unauthorized")))
        assertEquals("Registro não encontrado.", errorMessage(ApiException(404, "Brand 9 not found")))
        assertEquals("O servidor encontrou um erro. Tente novamente mais tarde.", errorMessage(ApiException(500, "boom")))
    }

    @Test
    fun otherFailuresNeverLeakTheTechnicalMessage() {
        // e.g. the HTTP client's "Failed to connect to /10.0.2.2:8080".
        assertEquals(
            "Não foi possível falar com o servidor. Verifique sua conexão e tente novamente.",
            errorMessage(RuntimeException("Failed to connect to /10.0.2.2:8080"))
        )
    }
}
