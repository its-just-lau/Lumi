
package com.lumi.app.data.repositories

import com.lumi.app.core.network.AuthService
import com.lumi.app.data.models.AuthResponse
import com.lumi.app.data.models.LoginData
import com.lumi.app.data.models.RegisterData

class AuthRepository(
    private val authService: AuthService
) {

    suspend fun register(
        data: RegisterData
    ): Result<AuthResponse> {
        return try {
            Result.success(authService.register(data))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun login(
        data: LoginData
    ): Result<AuthResponse> {
        return try {
            Result.success(authService.login(data))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
