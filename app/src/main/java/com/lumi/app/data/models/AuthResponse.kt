package com.lumi.app.data.models

/** Respuesta de register, login y refresh (Contrato de API §3). */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    // Vigencia del access token, en segundos.
    val expiresIn: Int,
    val user: AuthUser
)

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val timeZone: String,
    val version: Int? = null
)
