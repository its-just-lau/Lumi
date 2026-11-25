package com.lumi.app.features.study.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.learning.FlashcardResponse
import com.lumi.app.data.models.learning.LessonResponse
import com.lumi.app.data.models.learning.QuizAnswerInput
import com.lumi.app.data.models.learning.QuizAttemptInput
import com.lumi.app.data.models.learning.QuizAttemptResult
import com.lumi.app.data.models.learning.QuizResponse
import com.lumi.app.data.models.tasks.RewardResult
import com.lumi.app.data.repositories.MaterialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

data class LessonItem(val lesson: LessonResponse, val materialTitle: String)

data class QuizItem(val quiz: QuizResponse, val materialTitle: String)

data class LearningContent(
    val lessons: List<LessonItem> = emptyList(),
    val flashcards: List<FlashcardResponse> = emptyList(),
    val quizzes: List<QuizItem> = emptyList()
)

sealed interface LearningUiState {
    data object Loading : LearningUiState
    data class Success(val content: LearningContent) : LearningUiState
    data class Error(val message: String) : LearningUiState
}

sealed interface QuizSubmitState {
    data object Idle : QuizSubmitState
    data object Loading : QuizSubmitState
    data class Done(val result: QuizAttemptResult) : QuizSubmitState
    data class Error(val message: String) : QuizSubmitState
}

class LearningViewModel(
    private val repository: MaterialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Loading)
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    private val _quizState = MutableStateFlow<QuizSubmitState>(QuizSubmitState.Idle)
    val quizState: StateFlow<QuizSubmitState> = _quizState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Carga el contenido generado de todos los materiales ya procesados. */
    fun load() {
        viewModelScope.launch {
            repository.getAllMaterials()
                .onSuccess { materials ->
                    val lessons = mutableListOf<LessonItem>()
                    val flashcards = mutableListOf<FlashcardResponse>()
                    val quizzes = mutableListOf<QuizItem>()

                    materials
                        .filter { it.status == "completed" && it.deletedAt == null }
                        .forEach { material ->
                            repository.getLessons(material.id).getOrNull()?.forEach {
                                lessons.add(LessonItem(it, material.title))
                            }
                            repository.getFlashcards(material.id).getOrNull()?.let {
                                flashcards.addAll(it)
                            }
                            repository.getQuizzes(material.id).getOrNull()?.forEach {
                                quizzes.add(QuizItem(it, material.title))
                            }
                        }

                    _uiState.value = LearningUiState.Success(
                        LearningContent(lessons, flashcards, quizzes)
                    )
                }
                .onFailure {
                    _uiState.value = LearningUiState.Error(
                        it.toUserMessage(AppText.get(R.string.no_se_pudo_cargar_tu_2))
                    )
                }
        }
    }

    /** result: remembered | difficult | forgotten */
    fun reviewFlashcard(flashcardId: String, result: String) {
        viewModelScope.launch {
            repository.reviewFlashcard(
                flashcardId = flashcardId,
                idempotencyKey = UUID.randomUUID().toString(),
                reviewedAt = Instant.now().toString(),
                result = result
            ).onSuccess {
                rewardText(it.rewardResult)?.let { text -> _message.value = text }
            }.onFailure {
                _message.value = it.toUserMessage(AppText.get(R.string.no_se_pudo_guardar_la_2))
            }
        }
    }

    fun submitQuiz(quiz: QuizResponse, selected: Map<String, String>) {
        viewModelScope.launch {
            _quizState.value = QuizSubmitState.Loading

            val attempt = QuizAttemptInput(
                answers = quiz.questions.orEmpty().map {
                    QuizAnswerInput(
                        questionId = it.id,
                        selectedOptionIds = listOfNotNull(selected[it.id])
                    )
                }
            )

            repository.submitQuizAttempt(
                quizId = quiz.id,
                idempotencyKey = UUID.randomUUID().toString(),
                attempt = attempt
            ).onSuccess {
                _quizState.value = QuizSubmitState.Done(it)
                rewardText(it.rewardResult)?.let { text -> _message.value = text }
            }.onFailure {
                _quizState.value = QuizSubmitState.Error(
                    it.toUserMessage(AppText.get(R.string.no_se_pudo_enviar_el))
                )
            }
        }
    }

    fun resetQuiz() {
        _quizState.value = QuizSubmitState.Idle
    }

    fun completeLesson(lessonId: String) {
        viewModelScope.launch {
            repository.completeLesson(
                lessonId = lessonId,
                idempotencyKey = UUID.randomUUID().toString(),
                completedAt = Instant.now().toString()
            ).onSuccess {
                _message.value = rewardText(it.rewardResult) ?: AppText.get(R.string.leccion_completada)
            }.onFailure {
                _message.value = it.toUserMessage(AppText.get(R.string.no_se_pudo_completar_la))
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    private fun rewardText(reward: RewardResult?): String? {
        if (reward == null || reward.status != "confirmed") return null
        if (reward.xpAwarded <= 0 && reward.gemsAwarded <= 0) return null
        return AppText.get(R.string.xp_gemas, reward.xpAwarded, reward.gemsAwarded)
    }
}

class LearningViewModelFactory(
    private val repository: MaterialRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            return LearningViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
