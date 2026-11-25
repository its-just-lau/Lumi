package com.lumi.app.features.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.data.models.tasks.SubtaskInput
import com.lumi.app.data.models.tasks.TaskInput
import com.lumi.app.data.models.tasks.TaskResponse
import com.lumi.app.data.repositories.SubjectRepository
import com.lumi.app.data.repositories.TaskRepository
import com.lumi.app.features.tasks.model.TaskForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

sealed interface TasksUiState {
    data object Idle : TasksUiState
    data object Loading : TasksUiState

    data class Success(
        val tasks: List<TaskResponse>,
        val subjects: List<SubjectResponse>
    ) : TasksUiState

    data class Error(val message: String) : TasksUiState
}

sealed interface TaskDetailState {
    data object Idle : TaskDetailState
    data object Loading : TaskDetailState
    data class Success(val task: TaskResponse, val etag: String) : TaskDetailState
    data class Error(val message: String) : TaskDetailState
}

sealed interface TaskOperationState {
    data object Idle : TaskOperationState
    data object Loading : TaskOperationState
    data object Success : TaskOperationState
    data class Error(val message: String) : TaskOperationState
}

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Idle)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<TaskDetailState>(TaskDetailState.Idle)
    val detailState: StateFlow<TaskDetailState> = _detailState.asStateFlow()

    private val _operationState = MutableStateFlow<TaskOperationState>(TaskOperationState.Idle)
    val operationState: StateFlow<TaskOperationState> = _operationState.asStateFlow()

    // Mensaje corto para mostrar una vez (por ejemplo, la recompensa al completar).
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Se activa al completar una tarea para mostrar la celebración de Lumi.
    private val _completedEvent = MutableStateFlow(false)
    val completedEvent: StateFlow<Boolean> = _completedEvent.asStateFlow()

    // Tareas con una operación de completar/reabrir en curso (evita toques dobles).
    private val busyTaskIds = mutableSetOf<String>()

    // ----- Lista -----

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = TasksUiState.Loading

            val tasksResult = taskRepository.getAllTasks()

            if (tasksResult.isFailure) {
                _uiState.value = TasksUiState.Error(
                    tasksResult.exceptionOrNull()?.toUserMessage(
                        default = AppText.get(R.string.no_se_pudieron_cargar_las_2)
                    ) ?: AppText.get(R.string.no_se_pudieron_cargar_las_2)
                )
                return@launch
            }

            // Las materias solo sirven para mostrar su nombre y elegirlas en el formulario.
            val subjects = subjectRepository.getAllSubjects()
                .getOrNull()
                ?.filter { it.deletedAt == null && it.status == "active" }
                ?: emptyList()

            _uiState.value = TasksUiState.Success(
                tasks = tasksResult.getOrThrow().filter { it.deletedAt == null },
                subjects = subjects
            )
        }
    }

    // ----- Detalle (para obtener el ETag antes de editar) -----

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _detailState.value = TaskDetailState.Loading

            val result = taskRepository.getTask(taskId)

            result.onSuccess { response ->
                if (!response.isSuccessful) {
                    _detailState.value = TaskDetailState.Error(
                        when (response.code()) {
                            404 -> AppText.get(R.string.no_se_encontro_la_tarea)
                            410 -> AppText.get(R.string.la_tarea_fue_eliminada)
                            else -> AppText.get(R.string.error_al_consultar_la_tarea, response.code())
                        }
                    )
                    return@onSuccess
                }

                val task = response.body()
                val etag = response.headers()["ETag"]

                if (task == null || etag.isNullOrBlank()) {
                    _detailState.value = TaskDetailState.Error(
                        AppText.get(R.string.el_servidor_no_devolvio_los_2)
                    )
                    return@onSuccess
                }

                _detailState.value = TaskDetailState.Success(task, etag)
            }

            result.onFailure { error ->
                _detailState.value = TaskDetailState.Error(
                    error.toUserMessage(default = AppText.get(R.string.no_se_pudo_consultar_la_2))
                )
            }
        }
    }

    // ----- Crear / actualizar -----

    /** Si taskId es null crea una tarea nueva; si no, actualiza la existente con If-Match. */
    fun saveTask(taskId: String?, form: TaskForm) {
        if (form.title.isBlank()) {
            _operationState.value = TaskOperationState.Error(AppText.get(R.string.el_titulo_de_la_tarea))
            return
        }

        if (_operationState.value is TaskOperationState.Loading) return

        if (taskId == null) {
            createTask(form)
        } else {
            updateTask(taskId, form)
        }
    }

    private fun createTask(form: TaskForm) {
        viewModelScope.launch {
            _operationState.value = TaskOperationState.Loading

            val input = form.toInput(id = UUID.randomUUID().toString())

            taskRepository.createTask(input)
                .onSuccess { created ->
                    replaceInList(created)
                    _operationState.value = TaskOperationState.Success
                }
                .onFailure { error ->
                    _operationState.value = TaskOperationState.Error(
                        error.toUserMessage(default = AppText.get(R.string.no_se_pudo_crear_la_2))
                    )
                }
        }
    }

    private fun updateTask(taskId: String, form: TaskForm) {
        val detail = _detailState.value

        if (detail !is TaskDetailState.Success || detail.task.id != taskId) {
            _operationState.value = TaskOperationState.Error(
                AppText.get(R.string.primero_debes_consultar_la_tarea)
            )
            return
        }

        viewModelScope.launch {
            _operationState.value = TaskOperationState.Loading

            val result = taskRepository.updateTask(
                taskId = taskId,
                etag = detail.etag,
                task = form.toInput(id = taskId)
            )

            result.onSuccess { response ->
                if (!response.isSuccessful) {
                    _operationState.value = TaskOperationState.Error(
                        when (response.code()) {
                            409 -> AppText.get(R.string.esta_tarea_cambio_en_otro)
                            404 -> AppText.get(R.string.no_se_encontro_la_tarea)
                            410 -> AppText.get(R.string.la_tarea_fue_eliminada)
                            else -> AppText.get(R.string.error_al_actualizar_la_tarea, response.code())
                        }
                    )

                    // Conflicto: vuelve a consultar para tener el ETag vigente.
                    if (response.code() == 409) {
                        loadTask(taskId)
                    }
                    return@onSuccess
                }

                val updated = response.body()

                if (updated == null) {
                    loadTasks()
                } else {
                    replaceInList(updated)
                }

                _operationState.value = TaskOperationState.Success
            }

            result.onFailure { error ->
                _operationState.value = TaskOperationState.Error(
                    error.toUserMessage(default = AppText.get(R.string.no_se_pudo_actualizar_la_2))
                )
            }
        }
    }

    // ----- Eliminar -----

    fun deleteTask(taskId: String) {
        if (_operationState.value is TaskOperationState.Loading) return

        viewModelScope.launch {
            _operationState.value = TaskOperationState.Loading

            taskRepository.deleteTask(taskId)
                .onSuccess { response ->
                    if (response.isSuccessful || response.code() == 404 || response.code() == 410) {
                        removeFromList(taskId)
                        _operationState.value = TaskOperationState.Success
                    } else {
                        _operationState.value = TaskOperationState.Error(
                            AppText.get(R.string.error_al_eliminar_la_tarea, response.code())
                        )
                    }
                }
                .onFailure { error ->
                    _operationState.value = TaskOperationState.Error(
                        error.toUserMessage(default = AppText.get(R.string.no_se_pudo_eliminar_la_2))
                    )
                }
        }
    }

    // ----- Completar / reabrir -----

    fun toggleComplete(task: TaskResponse) {
        if (!busyTaskIds.add(task.id)) return

        viewModelScope.launch {
            val zone = ZoneId.systemDefault().id
            val key = UUID.randomUUID().toString()

            val result = if (task.isCompleted()) {
                taskRepository.reopenTask(task.id, key)
            } else {
                taskRepository.completeTask(
                    taskId = task.id,
                    idempotencyKey = key,
                    completedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                    timeZone = zone
                )
            }

            result.onSuccess { response ->
                val updated = response.task

                if (updated != null) {
                    replaceInList(updated)
                } else {
                    loadTasks()
                }

                _message.value = if (task.isCompleted()) {
                    AppText.get(R.string.tarea_reabierta)
                } else {
                    _completedEvent.value = true
                    val reward = response.rewardResult

                    if (reward != null && reward.status == "confirmed" &&
                        (reward.xpAwarded > 0 || reward.gemsAwarded > 0)
                    ) {
                        AppText.get(R.string.tarea_completada_xp_gemas, reward.xpAwarded, reward.gemsAwarded)
                    } else {
                        AppText.get(R.string.tarea_completada)
                    }
                }
            }

            result.onFailure { error ->
                _message.value = error.toUserMessage(
                    default = AppText.get(R.string.no_se_pudo_actualizar_la_2)
                )
            }

            busyTaskIds.remove(task.id)
        }
    }

    // ----- Estados -----

    fun resetOperationState() {
        _operationState.value = TaskOperationState.Idle
    }

    fun resetDetailState() {
        _detailState.value = TaskDetailState.Idle
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeCompleted() {
        _completedEvent.value = false
    }

    // ----- Utilidades -----

    private fun replaceInList(task: TaskResponse) {
        val current = _uiState.value

        if (current is TasksUiState.Success) {
            val exists = current.tasks.any { it.id == task.id }

            val updated = if (exists) {
                current.tasks.map { if (it.id == task.id) task else it }
            } else {
                current.tasks + task
            }

            _uiState.value = current.copy(tasks = updated)
        }
    }

    private fun removeFromList(taskId: String) {
        val current = _uiState.value

        if (current is TasksUiState.Success) {
            _uiState.value = current.copy(
                tasks = current.tasks.filterNot { it.id == taskId }
            )
        }
    }

    private fun TaskForm.toInput(id: String): TaskInput {
        return TaskInput(
            id = id,
            title = title.trim(),
            description = description.trim().ifBlank { null },
            subjectId = subjectId,
            dueDate = dueDate?.toString(),
            dueTime = if (allDay || dueDate == null) null else dueTime?.toString()?.take(5),
            allDay = allDay || dueTime == null,
            timeZone = ZoneId.systemDefault().id,
            priority = priority,
            subtasks = subtasks
                .filter { it.title.isNotBlank() }
                .map { SubtaskInput(id = it.id, title = it.title.trim(), completed = it.completed) }
        )
    }
}
