
package com.lumi.app.features.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.data.repositories.SubjectRepository
import com.lumi.app.features.subjects.model.SubjectInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SubjectsUiState {
    data object Idle : SubjectsUiState
    data object Loading : SubjectsUiState

    data class Success(
        val subjects: List<SubjectResponse>,
        val semesters: List<SemesterResponse>
    ) : SubjectsUiState

    data class Error(val message: String) : SubjectsUiState
}

sealed interface SubjectCreationState {
    data object Idle : SubjectCreationState
    data object Loading : SubjectCreationState
    data object Success : SubjectCreationState
    data class Error(val message: String) : SubjectCreationState
}

sealed interface SubjectDetailState {
    data object Idle : SubjectDetailState
    data object Loading : SubjectDetailState

    data class Success(
        val subject: SubjectResponse,
        val etag: String
    ) : SubjectDetailState

    data class Error(val message: String) : SubjectDetailState
}

sealed interface SubjectOperationState {
    data object Idle : SubjectOperationState
    data object Loading : SubjectOperationState
    data object Success : SubjectOperationState
    data class Error(val message: String) : SubjectOperationState
}

class SubjectViewModel(
    private val repository: SubjectRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<SubjectsUiState>(SubjectsUiState.Idle)

    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    private val _creationState =
        MutableStateFlow<SubjectCreationState>(SubjectCreationState.Idle)

    val creationState: StateFlow<SubjectCreationState> =
        _creationState.asStateFlow()

    private val _detailState =
        MutableStateFlow<SubjectDetailState>(SubjectDetailState.Idle)

    val detailState: StateFlow<SubjectDetailState> =
        _detailState.asStateFlow()

    private val _operationState =
        MutableStateFlow<SubjectOperationState>(SubjectOperationState.Idle)

    val operationState: StateFlow<SubjectOperationState> =
        _operationState.asStateFlow()


    // CARGAR MATERIAS Y SEMESTRES

    fun loadSubjects(
        authorization: String = "Bearer lumi-mock-token"
    ) {
        viewModelScope.launch {
            _uiState.value = SubjectsUiState.Loading

            val semestersResult =
                repository.getSemesters(authorization = authorization)

            val subjectsResult =
                repository.getSubjects(authorization = authorization)

            if (semestersResult.isSuccess && subjectsResult.isSuccess) {
                _uiState.value = SubjectsUiState.Success(
                    subjects = subjectsResult.getOrThrow().items,
                    semesters = semestersResult.getOrThrow().items
                )
            } else {
                val exception =
                    semestersResult.exceptionOrNull()
                        ?: subjectsResult.exceptionOrNull()

                _uiState.value = SubjectsUiState.Error(
                    exception?.message
                        ?: "No se pudieron cargar las materias."
                )
            }
        }
    }


    // CREAR MATERIA

    fun createSubject(
        name: String,
        description: String? = null,
        color: String? = null,
        semesterId: String? = null,
        authorization: String = "Bearer lumi-mock-token"
    ) {
        if (name.isBlank()) {
            _creationState.value =
                SubjectCreationState.Error(
                    "El nombre de la materia es obligatorio."
                )
            return
        }

        viewModelScope.launch {
            _creationState.value = SubjectCreationState.Loading

            val newSubject = SubjectInput(
                id = java.util.UUID.randomUUID().toString(),
                name = name.trim(),
                description = description?.trim()?.ifBlank { null },
                color = color,
                semesterId = semesterId,
                status = "active"
            )

            val result = repository.createSubject(
                authorization = authorization,
                subject = newSubject
            )

            result.onSuccess { createdSubject ->
                val currentState = _uiState.value

                if (currentState is SubjectsUiState.Success) {
                    val updatedSubjects =
                        currentState.subjects
                            .filterNot { it.id == createdSubject.id } +
                                createdSubject

                    _uiState.value =
                        currentState.copy(subjects = updatedSubjects)
                }

                _creationState.value = SubjectCreationState.Success
            }

            result.onFailure { error ->
                _creationState.value =
                    SubjectCreationState.Error(
                        error.message
                            ?: "No se pudo registrar la materia."
                    )
            }
        }
    }


    // CONSULTAR UNA MATERIA INDIVIDUAL

    fun loadSubject(
        subjectId: String,
        authorization: String = "Bearer lumi-mock-token"
    ) {
        viewModelScope.launch {
            _detailState.value = SubjectDetailState.Loading

            val result = repository.getSubject(
                authorization = authorization,
                subjectId = subjectId
            )

            result.onSuccess { response ->

                if (!response.isSuccessful) {
                    _detailState.value =
                        SubjectDetailState.Error(
                            when (response.code()) {
                                404 -> "No se encontró la materia."
                                410 -> "La materia fue eliminada."
                                else -> "Error al consultar la materia: ${response.code()}"
                            }
                        )
                    return@onSuccess
                }

                val subject = response.body()

                if (subject == null) {
                    _detailState.value =
                        SubjectDetailState.Error(
                            "El servidor no devolvió los datos de la materia."
                        )
                    return@onSuccess
                }

                val etag = response.headers()["ETag"]

                if (etag.isNullOrBlank()) {
                    _detailState.value =
                        SubjectDetailState.Error(
                            "El servidor no devolvió el ETag de la materia."
                        )
                    return@onSuccess
                }

                _detailState.value =
                    SubjectDetailState.Success(
                        subject = subject,
                        etag = etag
                    )
            }

            result.onFailure { error ->
                _detailState.value =
                    SubjectDetailState.Error(
                        error.message
                            ?: "No se pudo consultar la materia."
                    )
            }
        }
    }


    // ACTUALIZAR MATERIA CON ETAG

    fun updateSubject(
        subjectId: String,
        name: String,
        description: String? = null,
        color: String? = null,
        semesterId: String? = null,
        status: String = "active",
        authorization: String = "Bearer lumi-mock-token"
    ) {
        val detail = _detailState.value

        if (detail !is SubjectDetailState.Success ||
            detail.subject.id != subjectId
        ) {
            _operationState.value =
                SubjectOperationState.Error(
                    "Primero debes consultar la materia para obtener su ETag."
                )
            return
        }

        if (name.isBlank()) {
            _operationState.value =
                SubjectOperationState.Error(
                    "El nombre de la materia es obligatorio."
                )
            return
        }

        viewModelScope.launch {
            _operationState.value = SubjectOperationState.Loading

            val updatedSubject = SubjectInput(
                id = subjectId,
                name = name.trim(),
                description = description?.trim()?.ifBlank { null },
                color = color,
                semesterId = semesterId,
                status = status
            )

            val result = repository.updateSubject(
                authorization = authorization,
                subjectId = subjectId,
                etag = detail.etag,
                subject = updatedSubject
            )

            result.onSuccess { response ->

                if (!response.isSuccessful) {
                    _operationState.value =
                        SubjectOperationState.Error(
                            when (response.code()) {
                                409 -> "La materia fue modificada en otro dispositivo. Consulta nuevamente antes de actualizar."
                                404 -> "No se encontró la materia."
                                410 -> "La materia fue eliminada."
                                else -> "Error al actualizar la materia: ${response.code()}"
                            }
                        )
                    return@onSuccess
                }

                val updatedResponse = response.body()

                if (updatedResponse == null) {
                    _operationState.value =
                        SubjectOperationState.Error(
                            "La actualización fue aceptada, pero el servidor no devolvió la materia. Consulta nuevamente para verificarla."
                        )
                    return@onSuccess
                }

                val currentState = _uiState.value

                if (currentState is SubjectsUiState.Success) {
                    val updatedSubjects =
                        currentState.subjects.map { subject ->
                            if (subject.id == subjectId) {
                                updatedResponse
                            } else {
                                subject
                            }
                        }

                    _uiState.value =
                        currentState.copy(subjects = updatedSubjects)
                }

                _detailState.value = SubjectDetailState.Idle
                _operationState.value = SubjectOperationState.Success
            }

            result.onFailure { error ->
                _operationState.value =
                    SubjectOperationState.Error(
                        error.message
                            ?: "No se pudo actualizar la materia."
                    )
            }
        }
    }


    // ELIMINAR MATERIA

    fun deleteSubject(
        subjectId: String,
        authorization: String = "Bearer lumi-mock-token"
    ) {
        viewModelScope.launch {
            _operationState.value = SubjectOperationState.Loading

            val result = repository.deleteSubject(
                authorization = authorization,
                subjectId = subjectId
            )

            result.onSuccess { response ->

                if (!response.isSuccessful) {
                    _operationState.value =
                        SubjectOperationState.Error(
                            when (response.code()) {
                                404 -> "No se encontró la materia."
                                410 -> "La materia ya fue eliminada."
                                else -> "Error al eliminar la materia: ${response.code()}"
                            }
                        )
                    return@onSuccess
                }

                val currentState = _uiState.value

                if (currentState is SubjectsUiState.Success) {
                    val updatedSubjects =
                        currentState.subjects.filterNot {
                            it.id == subjectId
                        }

                    _uiState.value =
                        currentState.copy(subjects = updatedSubjects)
                }

                val currentDetail = _detailState.value

                if (currentDetail is SubjectDetailState.Success &&
                    currentDetail.subject.id == subjectId
                ) {
                    _detailState.value = SubjectDetailState.Idle
                }

                _operationState.value = SubjectOperationState.Success
            }

            result.onFailure { error ->
                _operationState.value =
                    SubjectOperationState.Error(
                        error.message
                            ?: "No se pudo eliminar la materia."
                    )
            }
        }
    }


    // REINICIAR ESTADOS

    fun resetState() {
        _uiState.value = SubjectsUiState.Idle
    }

    fun resetCreationState() {
        _creationState.value = SubjectCreationState.Idle
    }

    fun resetDetailState() {
        _detailState.value = SubjectDetailState.Idle
    }

    fun resetOperationState() {
        _operationState.value = SubjectOperationState.Idle
    }
}
