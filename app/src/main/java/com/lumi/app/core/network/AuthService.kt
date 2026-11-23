package com.lumi.app.core.network

import com.lumi.app.data.models.AuthResponse
import com.lumi.app.data.models.LoginData
import com.lumi.app.data.models.RegisterData

interface AuthService {

    suspend fun register(
        request: RegisterData
    ): AuthResponse

    suspend fun login(
        request: LoginData
    ): AuthResponse
}
