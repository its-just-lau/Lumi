package com.lumi.app.features.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.study.StudySessionInput
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.data.repositories.StudyRepository
import com.lumi.app.data.repositories.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

class StudyViewModel(
    private val studyRepository: StudyRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _subjects = MutableStateFlow<List<SubjectResponse>>(emptyList())
    val subjects: StateFlow<List<SubjectResponse>> = _subjects.asStateFlow()

    // Mensaje breve para mostrar una vez (recompensa o error al guardar la sesión)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadSubjects() {
        viewModelScope.launch {
            subjectRepository.getAllSubjects().onSuccess { list ->
                _subjects.value = list.filter {
                    it.deletedAt == null && it.status == "active"
                }
            }
        }
    }

    /** Registra una sesión terminada. mode: "pomodoro" o "free". */
    fun saveSession(
        subjectId: String?,
        isPomodoro: Boolean,
        plannedMinutes: Int?,
        pausedSeconds: Int,
        startedAt: Instant,
        endedAt: Instant
    ) {
        viewModelScope.launch {
            val input = StudySessionInput(
                id = UUID.randomUUID().toString(),
                subjectId = subjectId,
                mode = if (isPomodoro) "pomodoro" else "free",
                plannedMinutes = if (isPomodoro) plannedMinutes else null,
                pausedSeconds = pausedSeconds,
                startedAt = startedAt.toString(),
                endedAt = endedAt.toString(),
                timeZone = ZoneId.systemDefault().id
            )

            studyRepository.createStudySession(input)
                .onSuccess { result ->
                    val reward = result.rewardResult

                    _message.value = if (reward != null && reward.status == "confirmed" &&
                        (reward.xpAwarded > 0 || reward.gemsAwarded > 0)
                    ) {
                        AppText.get(R.string.sesion_guardada_xp_gemas, reward.xpAwarded, reward.gemsAwarded)
                    } else {
                        AppText.get(R.string.sesion_guardada)
                    }
                }
                .onFailure { error ->
                    _message.value = error.toUserMessage(
                        default = AppText.get(R.string.no_se_pudo_guardar_la)
                    )
                }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}

class StudyViewModelFactory(
    private val studyRepository: StudyRepository,
    private val subjectRepository: SubjectRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            return StudyViewModel(studyRepository, subjectRepository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
