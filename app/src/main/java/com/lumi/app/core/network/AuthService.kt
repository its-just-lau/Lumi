package com.lumi.app.core.network

import com.lumi.app.data.models.AuthResponse
import com.lumi.app.data.models.ForgotPasswordRequest
import com.lumi.app.data.models.GoogleLoginRequest
import com.lumi.app.data.models.LoginData
import com.lumi.app.data.models.RefreshRequest
import com.lumi.app.data.models.RegisterData
import com.lumi.app.data.models.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Endpoints públicos de autenticación (Contrato de API §3). */
interface AuthService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterData
    ): AuthResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginData
    ): AuthResponse

    // Propuesto para el contrato (ver docs/propuesta-auth-google.md)
    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest
    ): AuthResponse

    @POST("auth/logout")
    suspend fun logout(
        @Body request: RefreshRequest
    ): Response<Unit>

    @POST("auth/password/forgot")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<Unit>

    @POST("auth/password/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<Unit>
}
