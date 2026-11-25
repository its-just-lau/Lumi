package com.lumi.app.data.models.learning

import com.lumi.app.data.models.tasks.RewardResult

/** Materiales de estudio y contenido generado (Contrato de API §8). */

data class MaterialResponse(
    val id: String,
    val title: String,
    val type: String = "text",
    val status: String = "pending",
    val version: Int = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)

data class TextMaterialInput(
    val id: String,
    val title: String,
    val content: String
)

data class ProcessingStatusResponse(
    val materialId: String? = null,
    val processingJobId: String? = null,
    val status: String? = null,
    val progressPercent: Int = 0,
    val stage: String? = null
)

data class ItemsResponse<T>(
    val items: List<T>? = null
)

data class SourceReferenceResponse(
    val page: Int? = null,
    val quote: String? = null,
    val origin: String? = null
)

data class LearningExampleResponse(
    val title: String? = null,
    val content: String? = null,
    val origin: String? = null
)

data class LessonResponse(
    val id: String,
    val title: String,
    val explanation: String? = null,
    val origin: String? = null,
    val materialId: String? = null,
    val sourceReferences: List<SourceReferenceResponse>? = null,
    val examples: List<LearningExampleResponse>? = null
)

data class FlashcardResponse(
    val id: String,
    val question: String,
    val answer: String,
    val origin: String? = null,
    val sourcePage: Int? = null
)

data class QuizOptionResponse(
    val optionId: String,
    val text: String
)

data class QuizQuestionResponse(
    val id: String,
    val type: String? = null,
    val question: String,
    val options: List<QuizOptionResponse>? = null
)

data class QuizResponse(
    val id: String,
    val title: String? = null,
    val questions: List<QuizQuestionResponse>? = null
)

data class CompleteLessonRequest(val completedAt: String)

data class ReviewFlashcardRequest(
    val reviewedAt: String,
    val result: String
)

data class QuizAnswerInput(
    val questionId: String,
    val selectedOptionIds: List<String>
)

data class QuizAttemptInput(val answers: List<QuizAnswerInput>)

data class QuizAttemptResponse(
    val id: String? = null,
    val score: Double? = null,
    val submittedAt: String? = null
)

data class QuizFeedbackResponse(
    val questionId: String? = null,
    val correct: Boolean = false,
    val explanation: String? = null
)

data class QuizAttemptResult(
    val attempt: QuizAttemptResponse? = null,
    val feedback: List<QuizFeedbackResponse>? = null,
    val rewardResult: RewardResult? = null
)

data class LearningActionResponse(
    val progress: Map<String, Any?>? = null,
    val rewardResult: RewardResult? = null
)
