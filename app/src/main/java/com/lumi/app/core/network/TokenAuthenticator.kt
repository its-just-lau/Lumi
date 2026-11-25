package com.lumi.app.core.network

import com.lumi.app.core.storage.SessionManager
import com.lumi.app.data.models.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException

/**
 * Cuando una petición privada recibe 401, renueva el token UNA sola vez y
 * reintenta la petición UNA sola vez. Si varias peticiones fallan al mismo
 * tiempo, solo una renueva y las demás reutilizan el token nuevo.
 */
class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val authService: AuthService
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Los endpoints de autenticación (auth) nunca disparan renovación.
        if (AuthEndpoints.isPublic(response.request.url.encodedPath)) return null

        // Un solo reintento por petición.
        if (responseCount(response) >= 2) return null

        return synchronized(lock) {
            refreshAndRetry(response)
        }
    }

    private fun refreshAndRetry(response: Response): Request? {
        val usedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
        val currentToken = sessionManager.accessToken()

        // Otra petición ya renovó el token mientras esta esperaba.
        if (currentToken != null && currentToken != usedToken) {
            return response.request.withToken(currentToken)
        }

        val refreshToken = sessionManager.refreshToken() ?: return null

        return try {
            val auth = runBlocking {
                authService.refresh(RefreshRequest(refreshToken))
            }

            runBlocking {
                sessionManager.saveSession(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken
                )
            }

            response.request.withToken(auth.accessToken)
        } catch (exception: HttpException) {
            // El servidor rechazó el refresh token: la sesión terminó.
            if (exception.code() == 401) {
                runBlocking { sessionManager.expire() }
            }
            null
        } catch (exception: IOException) {
            // Sin red: no se cierra la sesión, simplemente falla esta petición.
            null
        } catch (exception: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private fun Request.withToken(token: String): Request {
        return newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}
