package com.lumi.app.data.models

data class RefreshRequest(
    val refreshToken: String
)

/** ID token que entrega Google al elegir una cuenta; el backend lo verifica. */
data class GoogleLoginRequest(
    val idToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
