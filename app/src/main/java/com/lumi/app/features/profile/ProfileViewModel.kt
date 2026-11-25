package com.lumi.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.data.repositories.ProgressRepository
import com.lumi.app.data.repositories.SubjectRepository
import com.lumi.app.data.repositories.TaskRepository
import com.lumi.app.data.repositories.UserRepository
import com.lumi.app.features.tasks.isCompleted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileData(
    val name: String = "",
    val email: String = "",
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val gems: Int = 0,
    val activeSubjects: Int = 0,
    val studyHours: Int = 0,
    val completedLessons: Int = 0,
    val completedTasks: Int = 0,
    // Minutos estudiados en cada uno de los últimos 7 días
    val weeklyMinutes: List<Int> = List(7) { 0 }
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _data = MutableStateFlow(ProfileData())
    val data: StateFlow<ProfileData> = _data.asStateFlow()

    /** Cada bloque se carga por separado: si uno falla, el resto sigue mostrándose. */
    fun load() {
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                _data.value = _data.value.copy(name = user.name, email = user.email)
            }
        }

        viewModelScope.launch {
            progressRepository.getWallet().onSuccess { wallet ->
                _data.value = _data.value.copy(totalXp = wallet.xp, gems = wallet.gems)
            }
            progressRepository.getStreak().onSuccess { streak ->
                _data.value = _data.value.copy(streakDays = streak.current)
            }
            progressRepository.getOverview().onSuccess { overview ->
                _data.value = _data.value.copy(
                    studyHours = overview.studyMinutes / 60,
                    completedLessons = overview.completedLessons
                )
            }
            progressRepository.getWeekly().onSuccess { weekly ->
                val padded = List((7 - weekly.dailyMinutes.size).coerceAtLeast(0)) { 0 } +
                        weekly.dailyMinutes
                _data.value = _data.value.copy(weeklyMinutes = padded)
            }
        }

        viewModelScope.launch {
            subjectRepository.getAllSubjects().onSuccess { subjects ->
                _data.value = _data.value.copy(
                    activeSubjects = subjects.count {
                        it.deletedAt == null && it.status == "active"
                    }
                )
            }
        }

        viewModelScope.launch {
            taskRepository.getAllTasks().onSuccess { tasks ->
                _data.value = _data.value.copy(
                    completedTasks = tasks.count { it.deletedAt == null && it.isCompleted() }
                )
            }
        }
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                userRepository,
                progressRepository,
                subjectRepository,
                taskRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
