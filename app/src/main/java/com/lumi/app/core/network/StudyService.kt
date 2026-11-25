package com.lumi.app.core.network

import com.lumi.app.data.models.study.StudySessionInput
import com.lumi.app.data.models.study.StudySessionResult
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface StudyService {

    // La Idempotency-Key evita duplicar la sesión (y su recompensa) si se reintenta.
    @POST("study-sessions")
    suspend fun createStudySession(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body session: StudySessionInput
    ): StudySessionResult
}
