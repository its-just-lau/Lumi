package com.lumi.app.core.network

import com.lumi.app.data.models.user.PreferencesResponse
import com.lumi.app.data.models.user.UpdateProfileRequest
import com.lumi.app.data.models.user.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface UserService {

    @GET("users/me")
    suspend fun getProfile(): UserProfileResponse

    /** Misma consulta, pero con cabeceras para leer el ETag que pide PUT /users/me. */
    @GET("users/me")
    suspend fun getProfileResponse(): Response<UserProfileResponse>

    @PUT("users/me")
    suspend fun updateProfile(
        @Header("If-Match") etag: String,
        @Body body: UpdateProfileRequest
    ): Response<UserProfileResponse>

    /** Solicita la eliminación de la cuenta y sus datos (202). */
    @DELETE("users/me")
    suspend fun deleteAccount(): Response<Unit>

    @GET("users/me/preferences")
    suspend fun getPreferences(): PreferencesResponse

    @PUT("users/me/preferences")
    suspend fun updatePreferences(@Body body: PreferencesResponse): PreferencesResponse
}
