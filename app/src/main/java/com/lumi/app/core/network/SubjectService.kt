package com.lumi.app.core.network

import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.features.subjects.model.SemesterInput
import com.lumi.app.features.subjects.model.SubjectInput

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.Path

interface SubjectService {

    // SEMESTERS

    @GET("semesters")
    suspend fun getSemesters(
        @Query("syncToken") syncToken: String? = null,
        @Query("syncRun") syncRun: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): PageResponse<SemesterResponse>


    // Crear semestre
    @POST("semesters")
    suspend fun createSemester(
        @Body semester: SemesterInput
    ): SemesterResponse


    // Consultar un semestre (devuelve el ETag)
    @GET("semesters/{semesterId}")
    suspend fun getSemester(
        @Path("semesterId") semesterId: String
    ): Response<SemesterResponse>


    // Actualizar semestre
    @PUT("semesters/{semesterId}")
    suspend fun updateSemester(
        @Path("semesterId") semesterId: String,
        @Header("If-Match") etag: String,
        @Body semester: SemesterInput
    ): Response<SemesterResponse>


    // Eliminar semestre (borrado lógico)
    @DELETE("semesters/{semesterId}")
    suspend fun deleteSemester(
        @Path("semesterId") semesterId: String
    ): Response<Unit>


    // SUBJECTS

    // Obtener todas las materias
    @GET("subjects")
    suspend fun getSubjects(
        @Query("syncToken") syncToken: String? = null,
        @Query("syncRun") syncRun: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): PageResponse<SubjectResponse>


    // Crear materia
    @POST("subjects")
    suspend fun createSubject(
        @Body subject: SubjectInput
    ): SubjectResponse


    // Consultar una materia específica
    @GET("subjects/{subjectId}")
    suspend fun getSubject(
        @Path("subjectId") subjectId: String
    ): Response<SubjectResponse>


    // Actualizar materia
    @PUT("subjects/{subjectId}")
    suspend fun updateSubject(
        @Path("subjectId") subjectId: String,
        @Header("If-Match") etag: String,
        @Body subject: SubjectInput
    ): Response<SubjectResponse>


    // Eliminar materia (borrado lógico)
    @DELETE("subjects/{subjectId}")
    suspend fun deleteSubject(
        @Path("subjectId") subjectId: String
    ): Response<Unit>
}
