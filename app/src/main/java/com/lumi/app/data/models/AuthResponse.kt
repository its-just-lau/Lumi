package com.lumi.app.data.models

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val user: AuthUser
)

data class AuthUser(
    val id: String,
    val name: String,
    val email: String
)
