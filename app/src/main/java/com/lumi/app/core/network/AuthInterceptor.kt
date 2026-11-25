package com.lumi.app.core.network

import com.lumi.app.core.storage.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/** Agrega `Authorization: Bearer <token>` a las peticiones privadas. */
class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = sessionManager.accessToken()

        if (token == null ||
            request.header("Authorization") != null ||
            AuthEndpoints.isPublic(request.url.encodedPath)
        ) {
            return chain.proceed(request)
        }

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
    }
}

/** Endpoints que no llevan token ni disparan la renovación automática. */
internal object AuthEndpoints {

    private val publicSuffixes = listOf(
        "auth/register",
        "auth/login",
        "auth/refresh",
        "auth/logout",
        "auth/password/forgot",
        "auth/password/reset"
    )

    fun isPublic(encodedPath: String): Boolean {
        return publicSuffixes.any { encodedPath.endsWith(it) }
    }
}
