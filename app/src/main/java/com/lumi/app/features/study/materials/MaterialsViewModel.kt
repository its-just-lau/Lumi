package com.lumi.app.features.study.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.learning.MaterialResponse
import com.lumi.app.data.models.learning.TextMaterialInput
import com.lumi.app.data.repositories.MaterialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

sealed interface MaterialsUiState {
    data object Idle : MaterialsUiState
    data object Loading : MaterialsUiState

    data class Success(
        val materials: List<MaterialResponse>,
        // Avance de procesamiento (0-100) por material mientras se generan lecciones, tarjetas y cuestionarios
        val progress: Map<String, Int> = emptyMap()
    ) : MaterialsUiState

    data class Error(val message: String) : MaterialsUiState
}

sealed interface MaterialOperationState {
    data object Idle : MaterialOperationState
    data object Loading : MaterialOperationState
    data object Success : MaterialOperationState
    data class Error(val message: String) : MaterialOperationState
}

class MaterialsViewModel(
    private val repository: MaterialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MaterialsUiState>(MaterialsUiState.Idle)
    val uiState: StateFlow<MaterialsUiState> = _uiState.asStateFlow()

    private val _operationState =
        MutableStateFlow<MaterialOperationState>(MaterialOperationState.Idle)
    val operationState: StateFlow<MaterialOperationState> = _operationState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var pollJob: Job? = null

    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.value = MaterialsUiState.Loading

            repository.getAllMaterials()
                .onSuccess { list ->
                    _uiState.value = MaterialsUiState.Success(
                        materials = list.filter { it.deletedAt == null }
                    )
                    ensurePolling()
                }
                .onFailure { error ->
                    _uiState.value = MaterialsUiState.Error(
                        error.toUserMessage(default = AppText.get(R.string.no_se_pudieron_cargar_tus_3))
                    )
                }
        }
    }

    fun createTextMaterial(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            _operationState.value = MaterialOperationState.Error(
                AppText.get(R.string.escribe_un_titulo_y_el)
            )
            return
        }

        if (_operationState.value is MaterialOperationState.Loading) return

        viewModelScope.launch {
            _operationState.value = MaterialOperationState.Loading

            val result = repository.createTextMaterial(
                TextMaterialInput(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    content = content.trim()
                )
            )

            handleCreated(result)
        }
    }

    fun uploadPdf(title: String, fileName: String, bytes: ByteArray) {
        if (title.isBlank()) {
            _operationState.value = MaterialOperationState.Error(AppText.get(R.string.escribe_un_titulo_para_el_2))
            return
        }

        if (_operationState.value is MaterialOperationState.Loading) return

        viewModelScope.launch {
            _operationState.value = MaterialOperationState.Loading

            handleCreated(repository.uploadPdf(title.trim(), fileName, bytes))
        }
    }

    // Al crear un material se pide de inmediato su procesamiento.
    private suspend fun handleCreated(result: Result<MaterialResponse>) {
        result
            .onSuccess { created ->
                addToList(created)

                repository.processMaterial(created.id)
                    .onSuccess { status ->
                        updateMaterial(created.id, status.status, status.progressPercent)
                    }
                    .onFailure { error ->
                        _message.value = error.toUserMessage(
                            default = AppText.get(R.string.el_material_se_guardo_pero)
                        )
                    }

                _operationState.value = MaterialOperationState.Success
                ensurePolling()
            }
            .onFailure { error ->
                _operationState.value = MaterialOperationState.Error(
                    error.toUserMessage(default = AppText.get(R.string.no_se_pudo_guardar_el_2))
                )
            }
    }

    /** Pide generar de nuevo el contenido: reintento si falló, o procesamiento si quedó pendiente. */
    fun processAgain(material: MaterialResponse) {
        viewModelScope.launch {
            val result = if (material.status == "failed") {
                repository.retryProcessing(material.id)
            } else {
                repository.processMaterial(material.id)
            }

            result
                .onSuccess { status ->
                    updateMaterial(material.id, status.status ?: "processing", status.progressPercent)
                    ensurePolling()
                }
                .onFailure { error ->
                    _message.value = error.toUserMessage(
                        default = AppText.get(R.string.no_se_pudo_procesar_el)
                    )
                }
        }
    }

    fun deleteMaterial(materialId: String) {
        if (_operationState.value is MaterialOperationState.Loading) return

        viewModelScope.launch {
            _operationState.value = MaterialOperationState.Loading

            repository.deleteMaterial(materialId)
                .onSuccess { response ->
                    if (response.isSuccessful || response.code() == 404 || response.code() == 410) {
                        removeFromList(materialId)
                        _operationState.value = MaterialOperationState.Success
                    } else {
                        _operationState.value = MaterialOperationState.Error(
                            AppText.get(R.string.error_al_eliminar_el_material, response.code())
                        )
                    }
                }
                .onFailure { error ->
                    _operationState.value = MaterialOperationState.Error(
                        error.toUserMessage(default = AppText.get(R.string.no_se_pudo_eliminar_el))
                    )
                }
        }
    }

    // ----- Seguimiento del procesamiento -----

    private fun ensurePolling() {
        if (pollJob?.isActive == true) return

        pollJob = viewModelScope.launch {
            var rounds = 0

            while (rounds < MAX_POLL_ROUNDS) {
                val state = _uiState.value as? MaterialsUiState.Success ?: break

                val active = state.materials.filter {
                    it.status == "pending" || it.status == "processing"
                }

                if (active.isEmpty()) break

                delay(POLL_INTERVAL_MS)

                active.forEach { material ->
                    repository.getProcessing(material.id).onSuccess { status ->
                        updateMaterial(material.id, status.status, status.progressPercent)
                    }
                }

                rounds++
            }
        }
    }

    private fun updateMaterial(materialId: String, status: String?, progressPercent: Int) {
        val current = _uiState.value as? MaterialsUiState.Success ?: return

        _uiState.value = current.copy(
            materials = current.materials.map {
                if (it.id == materialId && status != null) it.copy(status = status) else it
            },
            progress = current.progress + (materialId to progressPercent.coerceIn(0, 100))
        )
    }

    private fun addToList(material: MaterialResponse) {
        val current = _uiState.value

        _uiState.value = if (current is MaterialsUiState.Success) {
            current.copy(
                materials = current.materials.filterNot { it.id == material.id } + material
            )
        } else {
            MaterialsUiState.Success(materials = listOf(material))
        }
    }

    private fun removeFromList(materialId: String) {
        val current = _uiState.value as? MaterialsUiState.Success ?: return

        _uiState.value = current.copy(
            materials = current.materials.filterNot { it.id == materialId }
        )
    }

    fun resetOperationState() {
        _operationState.value = MaterialOperationState.Idle
    }

    fun consumeMessage() {
        _message.value = null
    }

    private companion object {
        const val POLL_INTERVAL_MS = 3000L
        const val MAX_POLL_ROUNDS = 200
    }
}

class MaterialsViewModelFactory(
    private val repository: MaterialRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaterialsViewModel::class.java)) {
            return MaterialsViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
