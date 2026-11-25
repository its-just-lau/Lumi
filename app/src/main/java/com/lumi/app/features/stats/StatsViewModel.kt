package com.lumi.app.features.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.data.repositories.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Datos del encabezado: gemas, XP y racha del usuario. */
data class UserStats(
    val gems: Int = 0,
    val xp: Int = 0,          // XP total desde que se creó la cuenta
    val weeklyXp: Int = 0,    // XP de la semana actual
    val streakDays: Int = 0
)

class StatsViewModel(
    private val repository: ProgressRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    /** Si una consulta falla se conserva el último valor conocido. */
    fun load() {
        viewModelScope.launch {
            repository.getWallet().onSuccess { wallet ->
                _stats.value = _stats.value.copy(gems = wallet.gems, xp = wallet.xp)
            }
            repository.getWeeklyXp().onSuccess { weekly ->
                _stats.value = _stats.value.copy(weeklyXp = weekly)
            }
            repository.getStreak().onSuccess { streak ->
                _stats.value = _stats.value.copy(streakDays = streak.current)
            }
        }
    }
}

class StatsViewModelFactory(
    private val repository: ProgressRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
