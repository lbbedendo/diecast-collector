package com.diecastcollector.app.ui

import com.diecastcollector.app.network.ApiException

/**
 * The text shown to the Collector when something fails. Exception messages (from the HTTP client
 * or the API) are English and technical, so they are never shown as-is: the message is chosen
 * here, in pt-BR, from the kind of failure. Pass [onConflict] where a 409 has a specific meaning
 * (e.g. a duplicate Series).
 */
internal fun errorMessage(error: Throwable, onConflict: String = "Já existe um registro igual."): String =
    when (error) {
        is ApiException -> when (error.status) {
            400 -> "Dados inválidos. Revise os campos e tente novamente."
            401, 403 -> "Sua sessão expirou. Entre novamente."
            404 -> "Registro não encontrado."
            409 -> onConflict
            else -> "O servidor encontrou um erro. Tente novamente mais tarde."
        }
        else -> "Não foi possível falar com o servidor. Verifique sua conexão e tente novamente."
    }
