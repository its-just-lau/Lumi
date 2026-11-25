package com.lumi.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.LoginData
import com.lumi.app.data.models.RegisterData
import com.lumi.app.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    fun register(data: RegisterData) {
        // Evita enviar dos veces el mismo formulario.
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.register(data).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success(
                        AppText.get(R.string.cuenta_creada_correctamente)
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_crear_la)
                        )
                    )
                }
            )
        }
    }

    fun login(data: LoginData) {
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.login(data).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success(
                        AppText.get(R.string.inicio_de_sesion_exitoso)
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_iniciar_sesion)
                        )
                    )
                }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.loginWithGoogle(idToken).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success(
                        AppText.get(R.string.inicio_de_sesion_exitoso)
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_iniciar_sesion_2)
                        )
                    )
                }
            )
        }
    }

    fun logout(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            onFinished()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
