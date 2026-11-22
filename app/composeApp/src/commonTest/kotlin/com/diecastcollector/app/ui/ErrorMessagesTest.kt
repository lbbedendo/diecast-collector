package com.diecastcollector.app.ui

import com.diecastcollector.app.auth.SignInFailure
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

    @Test
    fun signInFailuresMapToPortuguese() {
        assertEquals(
            "O login com o Google não está configurado neste app.",
            signInErrorMessage(SignInFailure(SignInFailure.Kind.NOT_CONFIGURED))
        )
        assertEquals(
            "Nenhuma conta Google neste aparelho. Adicione uma conta nas configurações e tente novamente.",
            signInErrorMessage(SignInFailure(SignInFailure.Kind.NO_ACCOUNT))
        )
        assertEquals(
            "Não foi possível entrar com o Google. Tente novamente.",
            signInErrorMessage(SignInFailure(SignInFailure.Kind.FAILED, RuntimeException("[16] Account reauth failed.")))
        )
    }

    @Test
    fun aRejectedIdTokenIsNotAnExpiredSession() {
        // Elsewhere a 401 means the app's own token expired; on login it means the API refused
        // the provider's ID token.
        assertEquals("O servidor não aceitou o login com o Google.", signInErrorMessage(ApiException(401, "Invalid Google ID token")))
        assertEquals(
            "Não foi possível falar com o servidor. Verifique sua conexão e tente novamente.",
            signInErrorMessage(RuntimeException("Failed to connect to /10.0.2.2:8080"))
        )
    }
}
