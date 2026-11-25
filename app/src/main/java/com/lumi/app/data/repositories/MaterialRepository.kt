package com.lumi.app.data.repositories

import com.lumi.app.core.network.MaterialService
import com.lumi.app.data.models.learning.CompleteLessonRequest
import com.lumi.app.data.models.learning.FlashcardResponse
import com.lumi.app.data.models.learning.LearningActionResponse
import com.lumi.app.data.models.learning.QuizAttemptInput
import com.lumi.app.data.models.learning.QuizAttemptResult
import com.lumi.app.data.models.learning.ReviewFlashcardRequest
import com.lumi.app.data.models.learning.LessonResponse
import com.lumi.app.data.models.learning.MaterialResponse
import com.lumi.app.data.models.learning.ProcessingStatusResponse
import com.lumi.app.data.models.learning.QuizResponse
import com.lumi.app.data.models.learning.TextMaterialInput
import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.core.storage.db.CacheStore
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class MaterialRepository(
    private val materialService: MaterialService,
    private val cache: CacheStore? = null
) {

    suspend fun getAllMaterials(): Result<List<MaterialResponse>> {
        val fetch: suspend () -> Result<List<MaterialResponse>> = { fetchAllMaterials() }

        return cache?.cached("materials", MaterialResponse::class.java, { it.id }, fetch)
            ?: fetch()
    }

    private suspend fun fetchAllMaterials(): Result<List<MaterialResponse>> {
        return safeCall {
            val all = mutableListOf<MaterialResponse>()
            var cursor: String? = null
            var pages = 0

            do {
                val page: PageResponse<MaterialResponse> =
                    materialService.getMaterials(cursor = cursor)
                all.addAll(page.items)

                cursor = if (page.hasMore) page.nextCursor else null
                pages++
            } while (cursor != null && pages < MAX_PAGES)

            all
        }
    }

    suspend fun createTextMaterial(material: TextMaterialInput): Result<MaterialResponse> {
        return safeCall { materialService.createTextMaterial(material) }
    }

    suspend fun uploadPdf(
        title: String,
        fileName: String,
        bytes: ByteArray
    ): Result<MaterialResponse> {
        return safeCall {
            val filePart = MultipartBody.Part.createFormData(
                "file",
                fileName,
                bytes.toRequestBody("application/pdf".toMediaType())
            )
            val titlePart = title.toRequestBody("text/plain".toMediaType())

            materialService.uploadPdf(filePart, titlePart)
        }
    }

    suspend fun deleteMaterial(materialId: String): Result<Response<Unit>> {
        return safeCall { materialService.deleteMaterial(materialId) }
    }

    suspend fun processMaterial(materialId: String): Result<ProcessingStatusResponse> {
        return safeCall { materialService.processMaterial(materialId) }
    }

    suspend fun retryProcessing(materialId: String): Result<ProcessingStatusResponse> {
        return safeCall { materialService.retryProcessing(materialId) }
    }

    suspend fun getProcessing(materialId: String): Result<ProcessingStatusResponse> {
        return safeCall { materialService.getProcessing(materialId) }
    }

    // ----- Contenido generado (se usará en lecciones, tarjetas y cuestionarios) -----

    suspend fun getLessons(materialId: String): Result<List<LessonResponse>> {
        return safeCall { materialService.getLessons(materialId).items.orEmpty() }
    }

    suspend fun getFlashcards(materialId: String): Result<List<FlashcardResponse>> {
        return safeCall { materialService.getFlashcards(materialId).items.orEmpty() }
    }

    suspend fun getQuizzes(materialId: String): Result<List<QuizResponse>> {
        return safeCall { materialService.getQuizzes(materialId).items.orEmpty() }
    }

    suspend fun reviewFlashcard(
        flashcardId: String,
        idempotencyKey: String,
        reviewedAt: String,
        result: String
    ): Result<LearningActionResponse> {
        return safeCall {
            materialService.reviewFlashcard(
                flashcardId,
                idempotencyKey,
                ReviewFlashcardRequest(reviewedAt = reviewedAt, result = result)
            )
        }
    }

    suspend fun submitQuizAttempt(
        quizId: String,
        idempotencyKey: String,
        attempt: QuizAttemptInput
    ): Result<QuizAttemptResult> {
        return safeCall { materialService.submitQuizAttempt(quizId, idempotencyKey, attempt) }
    }

    suspend fun completeLesson(
        lessonId: String,
        idempotencyKey: String,
        completedAt: String
    ): Result<LearningActionResponse> {
        return safeCall {
            materialService.completeLesson(
                lessonId,
                idempotencyKey,
                CompleteLessonRequest(completedAt = completedAt)
            )
        }
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

    private companion object {
        const val MAX_PAGES = 200
    }
}
