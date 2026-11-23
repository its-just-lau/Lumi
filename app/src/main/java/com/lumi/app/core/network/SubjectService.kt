package com.lumi.app.core.network

import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
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
        @Header("Authorization") authorization: String,
        @Query("syncToken") syncToken: String? = null,
        @Query("syncRun") syncRun: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): PageResponse<SemesterResponse>


    // SUBJECTS

    // Obtener todas las materias
    @GET("subjects")
    suspend fun getSubjects(
        @Header("Authorization") authorization: String,
        @Query("syncToken") syncToken: String? = null,
        @Query("syncRun") syncRun: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): PageResponse<SubjectResponse>


    // Crear materia
    @POST("subjects")
    suspend fun createSubject(
        @Header("Authorization") authorization: String,
        @Body subject: SubjectInput
    ): SubjectResponse


    // Consultar una materia específica
    @GET("subjects/{subjectId}")
    suspend fun getSubject(
        @Header("Authorization") authorization: String,
        @Path("subjectId") subjectId: String
    ): Response<SubjectResponse>


    // Actualizar materia
    @PUT("subjects/{subjectId}")
    suspend fun updateSubject(
        @Header("Authorization") authorization: String,
        @Path("subjectId") subjectId: String,
        @Header("If-Match") etag: String,
        @Body subject: SubjectInput
    ): Response<SubjectResponse>


    // Eliminar materia (borrado lógico)
    @DELETE("subjects/{subjectId}")
    suspend fun deleteSubject(
        @Header("Authorization") authorization: String,
        @Path("subjectId") subjectId: String
    ): Response<Unit>
}
