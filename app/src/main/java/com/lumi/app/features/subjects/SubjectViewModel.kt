
package com.lumi.app.features.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.data.repositories.SubjectRepository
import com.lumi.app.features.subjects.model.SemesterInput
import com.lumi.app.features.subjects.model.SubjectInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

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

sealed interface SemesterOperationState {
    data object Idle : SemesterOperationState
    data object Loading : SemesterOperationState
    data object Success : SemesterOperationState
    data class Error(val message: String) : SemesterOperationState
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

    private val _semesterOperationState =
        MutableStateFlow<SemesterOperationState>(SemesterOperationState.Idle)

    val semesterOperationState: StateFlow<SemesterOperationState> =
        _semesterOperationState.asStateFlow()


    // CARGAR MATERIAS Y SEMESTRES

    fun loadSubjects(
    ) {
        viewModelScope.launch {
            _uiState.value = SubjectsUiState.Loading

            val semestersResult =
                repository.getAllSemesters()

            val subjectsResult =
                repository.getAllSubjects()

            if (semestersResult.isSuccess && subjectsResult.isSuccess) {
                _uiState.value = SubjectsUiState.Success(
                    subjects = subjectsResult.getOrThrow()
                        .filter { it.deletedAt == null },
                    semesters = semestersResult.getOrThrow()
                        .filter { it.deletedAt == null }
                )
            } else {
                val exception =
                    semestersResult.exceptionOrNull()
                        ?: subjectsResult.exceptionOrNull()

                _uiState.value = SubjectsUiState.Error(
                    exception?.toUserMessage(
                        default = AppText.get(R.string.no_se_pudieron_cargar_las)
                    ) ?: AppText.get(R.string.no_se_pudieron_cargar_las)
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
    ) {
        if (name.isBlank()) {
            _creationState.value =
                SubjectCreationState.Error(
                    AppText.get(R.string.el_nombre_de_la_materia)
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
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_registrar_la)
                        )
                    )
            }
        }
    }


    // CONSULTAR UNA MATERIA INDIVIDUAL

    fun loadSubject(
        subjectId: String,
    ) {
        viewModelScope.launch {
            _detailState.value = SubjectDetailState.Loading

            val result = repository.getSubject(
                subjectId = subjectId
            )

            result.onSuccess { response ->

                if (!response.isSuccessful) {
                    _detailState.value =
                        SubjectDetailState.Error(
                            when (response.code()) {
                                404 -> AppText.get(R.string.no_se_encontro_la_materia)
                                410 -> AppText.get(R.string.la_materia_fue_eliminada)
                                else -> AppText.get(R.string.error_al_consultar_la_materia, response.code())
                            }
                        )
                    return@onSuccess
                }

                val subject = response.body()

                if (subject == null) {
                    _detailState.value =
                        SubjectDetailState.Error(
                            AppText.get(R.string.el_servidor_no_devolvio_los)
                        )
                    return@onSuccess
                }

                val etag = response.headers()["ETag"]

                if (etag.isNullOrBlank()) {
                    _detailState.value =
                        SubjectDetailState.Error(
                            AppText.get(R.string.el_servidor_no_devolvio_el)
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
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_consultar_la)
                        )
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
    ) {
        val detail = _detailState.value

        if (detail !is SubjectDetailState.Success ||
            detail.subject.id != subjectId
        ) {
            _operationState.value =
                SubjectOperationState.Error(
                    AppText.get(R.string.primero_debes_consultar_la_materia)
                )
            return
        }

        if (name.isBlank()) {
            _operationState.value =
                SubjectOperationState.Error(
                    AppText.get(R.string.el_nombre_de_la_materia)
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
                subjectId = subjectId,
                etag = detail.etag,
                subject = updatedSubject
            )

            result.onSuccess { response ->

                if (!response.isSuccessful) {
                    _operationState.value =
                        SubjectOperationState.Error(
                            when (response.code()) {
                                409 -> AppText.get(R.string.esta_materia_cambio_en_otro)
                                404 -> AppText.get(R.string.no_se_encontro_la_materia)
                                410 -> AppText.get(R.string.la_materia_fue_eliminada)
                                else -> AppText.get(R.string.error_al_actualizar_la_materia, response.code())
                            }
                        )

                    // Conflicto: vuelve a consultar para obtener el ETag vigente.
                    if (response.code() == 409) {
                        loadSubject(subjectId)
                    }

                    return@onSuccess
                }

                val updatedResponse = response.body()

                if (updatedResponse == null) {
                    _operationState.value =
                        SubjectOperationState.Error(
                            AppText.get(R.string.la_actualizacion_fue_aceptada_pero)
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
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_actualizar_la)
                        )
                    )
            }
        }
    }


    // ELIMINAR MATERIA

    fun deleteSubject(
        subjectId: String,
    ) {
        viewModelScope.launch {
            _operationState.value = SubjectOperationState.Loading

            val result = repository.deleteSubject(
                subjectId = subjectId
            )

            result.onSuccess { response ->

                if (!response.isSuccessful) {
                    _operationState.value =
                        SubjectOperationState.Error(
                            when (response.code()) {
                                404 -> AppText.get(R.string.no_se_encontro_la_materia)
                                410 -> AppText.get(R.string.la_materia_ya_fue_eliminada)
                                else -> AppText.get(R.string.error_al_eliminar_la_materia, response.code())
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
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_eliminar_la)
                        )
                    )
            }
        }
    }


    // CREAR SEMESTRE

    fun createSemester(name: String) {
        if (name.isBlank()) {
            _semesterOperationState.value =
                SemesterOperationState.Error(
                    AppText.get(R.string.el_nombre_del_semestre_es)
                )
            return
        }

        viewModelScope.launch {
            _semesterOperationState.value = SemesterOperationState.Loading

            val input = SemesterInput(
                id = java.util.UUID.randomUUID().toString(),
                name = name.trim()
            )

            val result = repository.createSemester(input)

            result.onSuccess { created ->
                val currentState = _uiState.value

                if (currentState is SubjectsUiState.Success) {
                    _uiState.value = currentState.copy(
                        semesters = currentState.semesters
                            .filterNot { it.id == created.id } + created
                    )
                }

                _semesterOperationState.value = SemesterOperationState.Success
            }

            result.onFailure { error ->
                _semesterOperationState.value =
                    SemesterOperationState.Error(
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_crear_el_2)
                        )
                    )
            }
        }
    }


    // RENOMBRAR SEMESTRE (consulta primero para obtener el ETag y conservar sus fechas)

    fun renameSemester(semesterId: String, name: String) {
        if (name.isBlank()) {
            _semesterOperationState.value =
                SemesterOperationState.Error(
                    AppText.get(R.string.el_nombre_del_semestre_es)
                )
            return
        }

        viewModelScope.launch {
            _semesterOperationState.value = SemesterOperationState.Loading

            val current = repository.getSemester(semesterId).getOrElse { error ->
                _semesterOperationState.value =
                    SemesterOperationState.Error(
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_consultar_el)
                        )
                    )
                return@launch
            }

            val semester = current.body()
            val etag = current.headers()["ETag"]

            if (!current.isSuccessful || semester == null || etag.isNullOrBlank()) {
                _semesterOperationState.value =
                    SemesterOperationState.Error(
                        if (current.code() == 404 || current.code() == 410) {
                            AppText.get(R.string.este_semestre_ya_no_existe)
                        } else {
                            AppText.get(R.string.no_se_pudo_consultar_el)
                        }
                    )
                return@launch
            }

            val input = SemesterInput(
                id = semesterId,
                name = name.trim(),
                startDate = semester.startDate,
                endDate = semester.endDate
            )

            val result = repository.updateSemester(semesterId, etag, input)

            result.onSuccess { response ->
                val updated = response.body()

                if (!response.isSuccessful || updated == null) {
                    _semesterOperationState.value =
                        SemesterOperationState.Error(
                            if (response.code() == 409) {
                                AppText.get(R.string.este_semestre_cambio_en_otro)
                            } else {
                                AppText.get(R.string.no_se_pudo_actualizar_el)
                            }
                        )
                    return@onSuccess
                }

                val currentState = _uiState.value

                if (currentState is SubjectsUiState.Success) {
                    _uiState.value = currentState.copy(
                        semesters = currentState.semesters.map {
                            if (it.id == semesterId) updated else it
                        }
                    )
                }

                _semesterOperationState.value = SemesterOperationState.Success
            }

            result.onFailure { error ->
                _semesterOperationState.value =
                    SemesterOperationState.Error(
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_actualizar_el)
                        )
                    )
            }
        }
    }


    // ELIMINAR SEMESTRE

    fun deleteSemester(semesterId: String) {
        val currentState = _uiState.value

        // El contrato no permite borrar un semestre que todavía tiene materias.
        if (currentState is SubjectsUiState.Success &&
            currentState.subjects.any { it.semesterId == semesterId }
        ) {
            _semesterOperationState.value =
                SemesterOperationState.Error(
                    AppText.get(R.string.este_semestre_todavia_tiene_materias)
                )
            return
        }

        viewModelScope.launch {
            _semesterOperationState.value = SemesterOperationState.Loading

            val result = repository.deleteSemester(semesterId)

            result.onSuccess { response ->
                if (!response.isSuccessful) {
                    _semesterOperationState.value =
                        SemesterOperationState.Error(
                            when (response.code()) {
                                404, 410 -> AppText.get(R.string.este_semestre_ya_fue_eliminado)
                                409 -> AppText.get(R.string.este_semestre_todavia_tiene_materias_2)
                                else -> AppText.get(R.string.no_se_pudo_eliminar_el_2)
                            }
                        )
                    return@onSuccess
                }

                val latestState = _uiState.value

                if (latestState is SubjectsUiState.Success) {
                    _uiState.value = latestState.copy(
                        semesters = latestState.semesters
                            .filterNot { it.id == semesterId }
                    )
                }

                _semesterOperationState.value = SemesterOperationState.Success
            }

            result.onFailure { error ->
                _semesterOperationState.value =
                    SemesterOperationState.Error(
                        error.toUserMessage(
                            default = AppText.get(R.string.no_se_pudo_eliminar_el_2)
                        )
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

    fun resetSemesterOperationState() {
        _semesterOperationState.value = SemesterOperationState.Idle
    }
}
