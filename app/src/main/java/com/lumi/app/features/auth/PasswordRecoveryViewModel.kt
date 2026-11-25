package com.lumi.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

/** Maneja los dos pasos de recuperación: pedir instrucciones y cambiar la contraseña. */
class PasswordRecoveryViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _forgotState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val forgotState: StateFlow<AuthUiState> =
        _forgotState.asStateFlow()

    private val _resetState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val resetState: StateFlow<AuthUiState> =
        _resetState.asStateFlow()

    fun forgotPassword(email: String) {
        if (_forgotState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _forgotState.value = AuthUiState.Loading

            repository.forgotPassword(email).fold(
                onSuccess = {
                    // El servidor no revela si el correo existe, así que el mensaje es neutro.
                    _forgotState.value = AuthUiState.Success(
                        AppText.get(R.string.si_el_correo_esta_registrado)
                    )
                },
                onFailure = { exception ->
                    _forgotState.value = AuthUiState.Error(
                        exception.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_enviar_la)
                        )
                    )
                }
            )
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        if (_resetState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _resetState.value = AuthUiState.Loading

            repository.resetPassword(token, newPassword).fold(
                onSuccess = {
                    _resetState.value = AuthUiState.Success(
                        AppText.get(R.string.tu_contrasena_se_actualizo_ya)
                    )
                },
                onFailure = { exception ->
                    _resetState.value = AuthUiState.Error(
                        exception.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_cambiar_la)
                        )
                    )
                }
            )
        }
    }

    fun clearResetState() {
        _resetState.value = AuthUiState.Idle
    }
}

class PasswordRecoveryViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(PasswordRecoveryViewModel::class.java)) {
            return PasswordRecoveryViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
