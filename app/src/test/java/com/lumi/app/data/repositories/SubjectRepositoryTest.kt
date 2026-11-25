package com.lumi.app.data.repositories

import com.lumi.app.core.network.SubjectService
import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.features.subjects.model.SemesterInput
import com.lumi.app.features.subjects.model.SubjectInput
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/** Servicio falso: devuelve páginas fijas y registra los cursores que se pidieron. */
private class FakeSubjectService(
    private val failWith: Exception? = null
) : SubjectService {

    val requestedCursors = mutableListOf<String?>()

    private fun subject(id: String) = SubjectResponse(
        id = id,
        name = "Materia $id",
        status = "active",
        version = 1
    )

    override suspend fun getSubjects(
        syncToken: String?,
        syncRun: String?,
        limit: Int,
        cursor: String?
    ): PageResponse<SubjectResponse> {
        failWith?.let { throw it }

        requestedCursors.add(cursor)

        return if (cursor == null) {
            PageResponse(
                items = listOf(subject("1"), subject("2")),
                nextCursor = "c2",
                hasMore = true,
                syncToken = "token",
                serverTime = "2026-11-23T00:00:00Z"
            )
        } else {
            PageResponse(
                items = listOf(subject("3")),
                nextCursor = null,
                hasMore = false,
                syncToken = "token",
                serverTime = "2026-11-23T00:00:00Z"
            )
        }
    }

    override suspend fun getSemesters(
        syncToken: String?,
        syncRun: String?,
        limit: Int,
        cursor: String?
    ): PageResponse<SemesterResponse> {
        requestedCursors.add(cursor)

        return PageResponse(
            items = listOf(SemesterResponse(id = "s1", name = "2026-1", version = 1)),
            nextCursor = null,
            hasMore = false,
            syncToken = "token",
            serverTime = "2026-11-23T00:00:00Z"
        )
    }

    override suspend fun createSemester(semester: SemesterInput): SemesterResponse =
        throw NotImplementedError()

    override suspend fun getSemester(semesterId: String): Response<SemesterResponse> =
        throw NotImplementedError()

    override suspend fun updateSemester(
        semesterId: String,
        etag: String,
        semester: SemesterInput
    ): Response<SemesterResponse> = throw NotImplementedError()

    override suspend fun deleteSemester(semesterId: String): Response<Unit> =
        throw NotImplementedError()

    override suspend fun createSubject(subject: SubjectInput): SubjectResponse =
        throw NotImplementedError()

    override suspend fun getSubject(subjectId: String): Response<SubjectResponse> =
        throw NotImplementedError()

    override suspend fun updateSubject(
        subjectId: String,
        etag: String,
        subject: SubjectInput
    ): Response<SubjectResponse> = throw NotImplementedError()

    override suspend fun deleteSubject(subjectId: String): Response<Unit> =
        throw NotImplementedError()
}

class SubjectRepositoryTest {

    @Test
    fun getAllSubjectsSigueLosCursoresHastaElFinal() = runBlocking {
        val service = FakeSubjectService()
        val repository = SubjectRepository(service)

        val result = repository.getAllSubjects()

        assertTrue(result.isSuccess)
        assertEquals(listOf("1", "2", "3"), result.getOrThrow().map { it.id })
        assertEquals(listOf<String?>(null, "c2"), service.requestedCursors)
    }

    @Test
    fun getAllSemestersDevuelveLaUnicaPagina() = runBlocking {
        val service = FakeSubjectService()
        val repository = SubjectRepository(service)

        val result = repository.getAllSemesters()

        assertEquals(listOf("s1"), result.getOrThrow().map { it.id })
    }

    @Test
    fun getAllSubjectsDevuelveFalloSiLaRedFalla() = runBlocking {
        val repository = SubjectRepository(FakeSubjectService(failWith = IOException("sin red")))

        val result = repository.getAllSubjects()

        assertTrue(result.isFailure)
    }
}
