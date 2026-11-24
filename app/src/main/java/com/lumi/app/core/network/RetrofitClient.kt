package com.lumi.app.core.network

import com.lumi.app.BuildConfig
import com.lumi.app.core.storage.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construye los clientes de red. La URL base sale de BuildConfig.BASE_URL, que
 * se configura en local.properties (ver app/build.gradle.kts).
 */
object RetrofitClient {

    private fun clientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
    }

    private fun retrofitFor(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Sin token ni renovación: se usa para los endpoints de autenticación (auth). */
    fun createPublicRetrofit(): Retrofit {
        return retrofitFor(clientBuilder().build())
    }

    /** Con token y renovación automática: se usa para el resto del API. */
    fun createAuthenticatedRetrofit(
        sessionManager: SessionManager,
        authService: AuthService
    ): Retrofit {
        val client = clientBuilder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(TokenAuthenticator(sessionManager, authService))
            .build()

        return retrofitFor(client)
    }
}
