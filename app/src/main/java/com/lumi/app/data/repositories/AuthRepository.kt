package com.lumi.app.data.repositories

import com.lumi.app.core.network.AuthService
import com.lumi.app.core.storage.SessionManager
import com.lumi.app.core.storage.db.CacheStore
import com.lumi.app.data.models.AuthResponse
import com.lumi.app.data.models.ForgotPasswordRequest
import com.lumi.app.data.models.GoogleLoginRequest
import com.lumi.app.data.models.LoginData
import com.lumi.app.data.models.RefreshRequest
import com.lumi.app.data.models.RegisterData
import com.lumi.app.data.models.ResetPasswordRequest
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class AuthRepository(
    private val authService: AuthService,
    private val sessionManager: SessionManager,
    private val cache: CacheStore? = null
) {

    suspend fun register(
        data: RegisterData
    ): Result<AuthResponse> {
        return apiCall {
            val auth = authService.register(data)
            startSession(auth)
            auth
        }
    }

    suspend fun login(
        data: LoginData
    ): Result<AuthResponse> {
        return apiCall {
            val auth = authService.login(data)
            startSession(auth)
            auth
        }
    }

    /** Inicia sesión (o crea la cuenta) con el ID token de Google. */
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return apiCall {
            val auth = authService.loginWithGoogle(GoogleLoginRequest(idToken))
            startSession(auth)
            auth
        }
    }

    /**
     * Cierra la sesión. Avisa al servidor si puede, pero siempre borra los
     * tokens locales, aunque no haya conexión.
     */
    suspend fun logout() {
        val refreshToken = sessionManager.refreshToken()

        if (refreshToken != null) {
            try {
                authService.logout(RefreshRequest(refreshToken))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                // Sin conexión o token ya revocado: igual se cierra localmente.
            }
        }

        sessionManager.clear()
        cache?.clearAll()
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return apiCall {
            val response = authService.forgotPassword(ForgotPasswordRequest(email))
            if (!response.isSuccessful) throw HttpException(response)
            Unit
        }
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String
    ): Result<Unit> {
        return apiCall {
            val response = authService.resetPassword(
                ResetPasswordRequest(token, newPassword)
            )
            if (!response.isSuccessful) throw HttpException(response)
            Unit
        }
    }

    // Guarda los tokens. Se hace aquí para que ninguna pantalla los maneje.
    private suspend fun startSession(auth: AuthResponse) {
        // Los datos guardados pertenecen a la cuenta anterior: se descartan
        cache?.clearAll()
        sessionManager.saveSession(
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken
        )
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
