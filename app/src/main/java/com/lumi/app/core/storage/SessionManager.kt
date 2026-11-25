package com.lumi.app.core.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SessionState {
    /** Todavía se está leyendo si hay una sesión guardada. */
    data object Loading : SessionState

    data object Authenticated : SessionState

    data object Unauthenticated : SessionState

    /** La sesión terminó porque el servidor ya no acepta el refresh token. */
    data object Expired : SessionState
}

/**
 * Fuente única de la sesión. El access token vive solo en memoria; el refresh
 * token se guarda cifrado en [TokenStorage].
 */
class SessionManager(
    private val tokenStorage: TokenStorage
) {

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    @Volatile
    private var accessTokenValue: String? = null

    @Volatile
    private var refreshTokenValue: String? = null

    fun accessToken(): String? = accessTokenValue

    fun refreshToken(): String? = refreshTokenValue

    /** Se llama al abrir la app para recuperar la sesión guardada. */
    suspend fun restore() {
        refreshTokenValue = tokenStorage.readRefreshToken()

        _state.value = if (refreshTokenValue != null) {
            SessionState.Authenticated
        } else {
            SessionState.Unauthenticated
        }
    }

    suspend fun saveSession(accessToken: String, refreshToken: String) {
        accessTokenValue = accessToken
        refreshTokenValue = refreshToken

        tokenStorage.saveRefreshToken(refreshToken)

        _state.value = SessionState.Authenticated
    }

    /** El servidor rechazó el refresh token: se pide iniciar sesión de nuevo. */
    suspend fun expire() {
        clearTokens()
        _state.value = SessionState.Expired
    }

    /** Cierre de sesión voluntario. */
    suspend fun clear() {
        clearTokens()
        _state.value = SessionState.Unauthenticated
    }

    private suspend fun clearTokens() {
        accessTokenValue = null
        refreshTokenValue = null
        tokenStorage.clear()
    }
}
