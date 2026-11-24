package com.lumi.app.data.repositories

import com.lumi.app.core.network.StudyService
import com.lumi.app.data.models.study.StudySessionInput
import com.lumi.app.data.models.study.StudySessionResult
import kotlinx.coroutines.CancellationException

class StudyRepository(
    private val studyService: StudyService
) {

    /** El id de la sesión se usa también como Idempotency-Key: un reintento no duplica nada. */
    suspend fun createStudySession(
        session: StudySessionInput
    ): Result<StudySessionResult> {
        return try {
            Result.success(studyService.createStudySession(session.id, session))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
