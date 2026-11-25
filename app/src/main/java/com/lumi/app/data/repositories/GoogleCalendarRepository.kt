package com.lumi.app.data.repositories

import com.lumi.app.core.network.GoogleCalendarService
import com.lumi.app.data.models.calendar.GoogleEvent
import com.lumi.app.data.models.calendar.GoogleEventInput
import kotlinx.coroutines.CancellationException

class GoogleCalendarRepository(
    private val service: GoogleCalendarService
) {

    suspend fun listEvents(
        accessToken: String,
        timeMin: String,
        timeMax: String
    ): Result<List<GoogleEvent>> {
        return safeCall {
            service.listEvents("Bearer $accessToken", timeMin, timeMax).items.orEmpty()
        }
    }

    suspend fun createEvent(
        accessToken: String,
        event: GoogleEventInput
    ): Result<GoogleEvent> {
        return safeCall { service.insertEvent("Bearer $accessToken", event) }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
