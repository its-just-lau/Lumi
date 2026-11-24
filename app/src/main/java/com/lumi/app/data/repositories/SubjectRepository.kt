package com.lumi.app.data.repositories

import com.lumi.app.core.network.SubjectService
import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.features.subjects.model.SemesterInput
import com.lumi.app.features.subjects.model.SubjectInput
import com.lumi.app.core.storage.db.CacheStore
import kotlinx.coroutines.CancellationException

import retrofit2.Response

class SubjectRepository(
    private val subjectService: SubjectService,
    private val cache: CacheStore? = null
) {

    // Obtener semestres
    suspend fun getSemesters(
        syncToken: String? = null,
        syncRun: String? = null
    ): Result<PageResponse<SemesterResponse>> {
        return try {
            Result.success(
                subjectService.getSemesters(
                    syncToken = syncToken,
                    syncRun = syncRun
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Obtener todas las materias
    suspend fun getSubjects(
        syncToken: String? = null,
        syncRun: String? = null
    ): Result<PageResponse<SubjectResponse>> {
        return try {
            Result.success(
                subjectService.getSubjects(
                    syncToken = syncToken,
                    syncRun = syncRun
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Crear materia
    suspend fun createSubject(
        subject: SubjectInput
    ): Result<SubjectResponse> {
        return try {
            Result.success(
                subjectService.createSubject(
                    subject = subject
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Consultar una materia específica
    suspend fun getSubject(
        subjectId: String
    ): Result<Response<SubjectResponse>> {
        return try {
            Result.success(
                subjectService.getSubject(
                    subjectId = subjectId
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Actualizar materia
    suspend fun updateSubject(
        subjectId: String,
        etag: String,
        subject: SubjectInput
    ): Result<Response<SubjectResponse>> {
        return try {
            Result.success(
                subjectService.updateSubject(
                    subjectId = subjectId,
                    etag = etag,
                    subject = subject
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Eliminar materia
    suspend fun deleteSubject(
        subjectId: String
    ): Result<Response<Unit>> {
        return try {
            Result.success(
                subjectService.deleteSubject(
                    subjectId = subjectId
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    // ----- Todas las páginas (sigue nextCursor hasta que no haya más) -----

    suspend fun getAllSemesters(): Result<List<SemesterResponse>> {
        val fetch: suspend () -> Result<List<SemesterResponse>> = {
            fetchAllPages { cursor -> subjectService.getSemesters(cursor = cursor) }
        }

        return cache?.cached("semesters", SemesterResponse::class.java, { it.id }, fetch)
            ?: fetch()
    }

    suspend fun getAllSubjects(): Result<List<SubjectResponse>> {
        val fetch: suspend () -> Result<List<SubjectResponse>> = {
            fetchAllPages { cursor -> subjectService.getSubjects(cursor = cursor) }
        }

        return cache?.cached("subjects", SubjectResponse::class.java, { it.id }, fetch)
            ?: fetch()
    }

    private suspend fun <T> fetchAllPages(
        fetchPage: suspend (String?) -> PageResponse<T>
    ): Result<List<T>> {
        return try {
            val all = mutableListOf<T>()
            var cursor: String? = null
            var pages = 0

            do {
                val page = fetchPage(cursor)
                all.addAll(page.items)

                cursor = if (page.hasMore) page.nextCursor else null
                pages++
            } while (cursor != null && pages < MAX_PAGES)

            Result.success(all)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // ----- Semestres -----

    suspend fun createSemester(
        semester: SemesterInput
    ): Result<SemesterResponse> {
        return safeCall { subjectService.createSemester(semester) }
    }

    suspend fun getSemester(
        semesterId: String
    ): Result<Response<SemesterResponse>> {
        return safeCall { subjectService.getSemester(semesterId) }
    }

    suspend fun updateSemester(
        semesterId: String,
        etag: String,
        semester: SemesterInput
    ): Result<Response<SemesterResponse>> {
        return safeCall {
            subjectService.updateSemester(semesterId, etag, semester)
        }
    }

    suspend fun deleteSemester(
        semesterId: String
    ): Result<Response<Unit>> {
        return safeCall { subjectService.deleteSemester(semesterId) }
    }

    private suspend fun <T> safeCall(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private companion object {
        // Tope de seguridad por si el servidor devolviera siempre el mismo cursor.
        const val MAX_PAGES = 200
    }
}
