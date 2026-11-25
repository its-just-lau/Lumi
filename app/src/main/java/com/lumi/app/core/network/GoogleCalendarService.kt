package com.lumi.app.core.network

import com.lumi.app.data.models.calendar.GoogleEvent
import com.lumi.app.data.models.calendar.GoogleEventInput
import com.lumi.app.data.models.calendar.GoogleEventsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API REST de Google Calendar v3. Usa su propio Retrofit (sin el interceptor de Lumi)
 * para que los tokens de Lumi nunca se envíen a Google.
 */
interface GoogleCalendarService {

    @GET("calendars/primary/events")
    suspend fun listEvents(
        @Header("Authorization") authorization: String,
        @Query("timeMin") timeMin: String,
        @Query("timeMax") timeMax: String,
        @Query("singleEvents") singleEvents: Boolean = true,
        @Query("orderBy") orderBy: String = "startTime",
        @Query("maxResults") maxResults: Int = 250
    ): GoogleEventsResponse

    @POST("calendars/primary/events")
    suspend fun insertEvent(
        @Header("Authorization") authorization: String,
        @Body event: GoogleEventInput
    ): GoogleEvent
}
