package com.lumi.app.data.repositories

import com.lumi.app.core.network.SubjectService
import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.features.subjects.model.SubjectInput

import retrofit2.Response

class SubjectRepository(
    private val subjectService: SubjectService
) {

    // Obtener semestres
    suspend fun getSemesters(
        authorization: String,
        syncToken: String? = null,
        syncRun: String? = null
    ): Result<PageResponse<SemesterResponse>> {
        return try {
            Result.success(
                subjectService.getSemesters(
                    authorization = authorization,
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
        authorization: String,
        syncToken: String? = null,
        syncRun: String? = null
    ): Result<PageResponse<SubjectResponse>> {
        return try {
            Result.success(
                subjectService.getSubjects(
                    authorization = authorization,
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
        authorization: String,
        subject: SubjectInput
    ): Result<SubjectResponse> {
        return try {
            Result.success(
                subjectService.createSubject(
                    authorization = authorization,
                    subject = subject
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Consultar una materia específica
    suspend fun getSubject(
        authorization: String,
        subjectId: String
    ): Result<Response<SubjectResponse>> {
        return try {
            Result.success(
                subjectService.getSubject(
                    authorization = authorization,
                    subjectId = subjectId
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Actualizar materia
    suspend fun updateSubject(
        authorization: String,
        subjectId: String,
        etag: String,
        subject: SubjectInput
    ): Result<Response<SubjectResponse>> {
        return try {
            Result.success(
                subjectService.updateSubject(
                    authorization = authorization,
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
        authorization: String,
        subjectId: String
    ): Result<Response<Unit>> {
        return try {
            Result.success(
                subjectService.deleteSubject(
                    authorization = authorization,
                    subjectId = subjectId
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
