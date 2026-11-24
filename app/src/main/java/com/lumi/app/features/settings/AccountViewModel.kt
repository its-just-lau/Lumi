package com.lumi.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.data.models.user.PreferencesResponse
import com.lumi.app.data.repositories.UserRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

const val DEFAULT_DAILY_GOAL_MINUTES = 60

data class AccountUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val name: String = "",
    val email: String = "",
    val timeZone: String = "",
    val etag: String? = null,
    val dailyGoalMinutes: Int = DEFAULT_DAILY_GOAL_MINUTES,
    val goals: List<String> = emptyList(),
    val message: String? = null,
    /** Se pone en true cuando un guardado terminó bien, para cerrar la pantalla. */
    val saved: Boolean = false
)

class AccountViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            userRepository.getProfileWithEtag()
                .onSuccess { result ->
                    _state.value = _state.value.copy(
                        loading = false,
                        name = result.profile.name,
                        email = result.profile.email,
                        timeZone = result.profile.timeZone,
                        etag = result.etag
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        message = errorText(it, AppText.get(R.string.no_se_pudo_cargar_tu))
                    )
                }
        }
    }

    fun saveProfile(name: String) {
        val trimmed = name.trim()
        val etag = _state.value.etag
        if (trimmed.isEmpty()) {
            _state.value = _state.value.copy(message = AppText.get(R.string.escribe_tu_nombre))
            return
        }
        if (etag.isNullOrBlank()) {
            _state.value = _state.value.copy(message = AppText.get(R.string.espera_a_que_cargue_tu))
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            userRepository.updateProfile(etag, trimmed)
                .onSuccess { result ->
                    _state.value = _state.value.copy(
                        saving = false,
                        name = result.profile.name,
                        etag = result.etag,
                        message = AppText.get(R.string.perfil_actualizado),
                        saved = true
                    )
                }
                .onFailure {
                    val conflict = (it as? HttpException)?.code() == 409
                    _state.value = _state.value.copy(
                        saving = false,
                        message = if (conflict) {
                            AppText.get(R.string.tu_perfil_cambio_en_otro)
                        } else {
                            errorText(it, AppText.get(R.string.no_se_pudo_guardar_el))
                        }
                    )
                    if (conflict) loadProfile()
                }
        }
    }

    fun loadPreferences() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            userRepository.getPreferences()
                .onSuccess { prefs ->
                    _state.value = _state.value.copy(
                        loading = false,
                        dailyGoalMinutes = prefs.dailyGoalMinutes ?: DEFAULT_DAILY_GOAL_MINUTES,
                        goals = prefs.selectedGoals.orEmpty()
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        message = errorText(it, AppText.get(R.string.no_se_pudieron_cargar_tus_2))
                    )
                }
        }
    }

    fun savePreferences(dailyGoalMinutes: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            userRepository.updatePreferences(
                PreferencesResponse(
                    selectedGoals = _state.value.goals,
                    dailyGoalMinutes = dailyGoalMinutes
                )
            )
                .onSuccess { prefs ->
                    _state.value = _state.value.copy(
                        saving = false,
                        dailyGoalMinutes = prefs.dailyGoalMinutes ?: dailyGoalMinutes,
                        message = AppText.get(R.string.preferencias_guardadas),
                        saved = true
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        saving = false,
                        message = errorText(it, AppText.get(R.string.no_se_pudieron_guardar_las))
                    )
                }
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            userRepository.deleteAccount()
                .onSuccess {
                    _state.value = _state.value.copy(saving = false)
                    onDone()
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        saving = false,
                        message = errorText(it, AppText.get(R.string.no_se_pudo_solicitar_la))
                    )
                }
        }
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun consumeSaved() {
        _state.value = _state.value.copy(saved = false)
    }

    private fun errorText(error: Throwable, fallback: String): String {
        return if (error is IOException) {
            AppText.get(R.string.sin_conexion_revisa_tu_internet)
        } else {
            fallback
        }
    }
}

class AccountViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(userRepository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
