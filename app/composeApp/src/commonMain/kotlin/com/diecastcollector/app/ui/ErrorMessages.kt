package com.diecastcollector.app.ui

import com.diecastcollector.app.auth.SignInFailure
import com.diecastcollector.app.network.ApiException

/**
 * The text shown to the Collector when something fails. Exception messages (from the HTTP client
 * or the API) are English and technical, so they are never shown as-is: the message is chosen
 * here, in pt-BR, from the kind of failure. Pass [onConflict] where a 409 has a specific meaning
 * (e.g. a duplicate Series).
 */
/** Shown when a Model was saved but its new photo didn't reach the API. */
internal const val PHOTO_NOT_SENT = "O modelo foi salvo, mas não foi possível enviar a foto. Tente de novo editando o modelo."

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

/**
 * The text shown when signing in fails, either in the provider's flow ([SignInFailure]) or when
 * the API turns the provider's ID token down — which, unlike on any other call, is not an expired
 * session.
 */
internal fun signInErrorMessage(error: Throwable): String =
    when {
        error is SignInFailure -> when (error.kind) {
            SignInFailure.Kind.NOT_CONFIGURED -> "O login com o Google não está configurado neste app."
            SignInFailure.Kind.NO_ACCOUNT -> "Nenhuma conta Google neste aparelho. Adicione uma conta nas configurações e tente novamente."
            SignInFailure.Kind.FAILED -> "Não foi possível entrar com o Google. Tente novamente."
        }
        error is ApiException && error.status == 401 -> "O servidor não aceitou o login com o Google."
        else -> errorMessage(error)
    }
