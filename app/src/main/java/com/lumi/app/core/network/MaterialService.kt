package com.lumi.app.core.network

import com.lumi.app.data.models.learning.CompleteLessonRequest
import com.lumi.app.data.models.learning.FlashcardResponse
import com.lumi.app.data.models.learning.ItemsResponse
import com.lumi.app.data.models.learning.LearningActionResponse
import com.lumi.app.data.models.learning.LessonResponse
import com.lumi.app.data.models.learning.MaterialResponse
import com.lumi.app.data.models.learning.ProcessingStatusResponse
import com.lumi.app.data.models.learning.QuizAttemptInput
import com.lumi.app.data.models.learning.QuizAttemptResult
import com.lumi.app.data.models.learning.QuizResponse
import com.lumi.app.data.models.learning.ReviewFlashcardRequest
import com.lumi.app.data.models.learning.TextMaterialInput
import com.lumi.app.data.models.subjects.PageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MaterialService {

    @GET("materials")
    suspend fun getMaterials(
        @Query("syncToken") syncToken: String? = null,
        @Query("syncRun") syncRun: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): PageResponse<MaterialResponse>

    @POST("materials")
    suspend fun createTextMaterial(
        @Body material: TextMaterialInput
    ): MaterialResponse

    @Multipart
    @POST("materials/upload")
    suspend fun uploadPdf(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody
    ): MaterialResponse

    @GET("materials/{materialId}")
    suspend fun getMaterial(
        @Path("materialId") materialId: String
    ): MaterialResponse

    @DELETE("materials/{materialId}")
    suspend fun deleteMaterial(
        @Path("materialId") materialId: String
    ): Response<Unit>

    // Encola el procesamiento (el servidor genera lecciones, tarjetas y cuestionarios)
    @POST("materials/{materialId}/process")
    suspend fun processMaterial(
        @Path("materialId") materialId: String
    ): ProcessingStatusResponse

    @GET("materials/{materialId}/processing")
    suspend fun getProcessing(
        @Path("materialId") materialId: String
    ): ProcessingStatusResponse

    @POST("materials/{materialId}/retry")
    suspend fun retryProcessing(
        @Path("materialId") materialId: String
    ): ProcessingStatusResponse

    // ----- Contenido generado -----

    @GET("materials/{materialId}/lessons")
    suspend fun getLessons(
        @Path("materialId") materialId: String
    ): ItemsResponse<LessonResponse>

    @GET("materials/{materialId}/flashcards")
    suspend fun getFlashcards(
        @Path("materialId") materialId: String
    ): ItemsResponse<FlashcardResponse>

    @GET("materials/{materialId}/quizzes")
    suspend fun getQuizzes(
        @Path("materialId") materialId: String
    ): ItemsResponse<QuizResponse>

    @GET("lessons/{lessonId}")
    suspend fun getLesson(
        @Path("lessonId") lessonId: String
    ): LessonResponse

    @POST("lessons/{lessonId}/complete")
    suspend fun completeLesson(
        @Path("lessonId") lessonId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CompleteLessonRequest
    ): LearningActionResponse

    @POST("flashcards/{flashcardId}/reviews")
    suspend fun reviewFlashcard(
        @Path("flashcardId") flashcardId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: ReviewFlashcardRequest
    ): LearningActionResponse

    @POST("quizzes/{quizId}/attempts")
    suspend fun submitQuizAttempt(
        @Path("quizId") quizId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: QuizAttemptInput
    ): QuizAttemptResult
}
