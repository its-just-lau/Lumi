package com.lumi.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumi.app.data.models.LoginData
import com.lumi.app.data.models.RegisterData
import com.lumi.app.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    fun register(data: RegisterData) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.register(data).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success(
                        "Cuenta creada correctamente."
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.message ?: "No se pudo crear la cuenta."
                    )
                }
            )
        }
    }

    fun login(data: LoginData) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            repository.login(data).fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success(
                        "Inicio de sesión exitoso."
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.message ?: "No se pudo iniciar sesión."
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
