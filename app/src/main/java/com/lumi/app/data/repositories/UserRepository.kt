package com.lumi.app.data.repositories

import com.lumi.app.core.network.UserService
import com.lumi.app.data.models.user.PreferencesResponse
import com.lumi.app.data.models.user.UpdateProfileRequest
import com.lumi.app.data.models.user.UserProfileResponse
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

/** Perfil junto con el ETag que se debe enviar en If-Match al editarlo. */
data class ProfileWithEtag(
    val profile: UserProfileResponse,
    val etag: String?
)

class UserRepository(
    private val userService: UserService
) {

    private suspend fun <T> safe(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        val body = body()
        if (!isSuccessful || body == null) throw HttpException(this)
        return body
    }

    suspend fun getPreferences(): Result<PreferencesResponse> = safe {
        userService.getPreferences()
    }

    suspend fun getProfile(): Result<UserProfileResponse> = safe {
        userService.getProfile()
    }

    suspend fun getProfileWithEtag(): Result<ProfileWithEtag> = safe {
        val response = userService.getProfileResponse()
        val profile = response.bodyOrThrow()
        ProfileWithEtag(
            profile = profile,
            etag = response.headers()["ETag"] ?: profile.version?.let { "\"$it\"" }
        )
    }

    suspend fun updateProfile(
        etag: String,
        name: String,
        timeZone: String? = null
    ): Result<ProfileWithEtag> = safe {
        val response = userService.updateProfile(etag, UpdateProfileRequest(name, timeZone))
        val profile = response.bodyOrThrow()
        ProfileWithEtag(
            profile = profile,
            etag = response.headers()["ETag"] ?: profile.version?.let { "\"$it\"" }
        )
    }

    suspend fun updatePreferences(preferences: PreferencesResponse): Result<PreferencesResponse> =
        safe { userService.updatePreferences(preferences) }

    suspend fun deleteAccount(): Result<Unit> = safe {
        val response = userService.deleteAccount()
        if (!response.isSuccessful) throw HttpException(response)
    }
}
