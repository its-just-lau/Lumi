package com.lumi.app.features.tasks.model

import java.time.LocalDate
import java.time.LocalTime

/** Subtarea mientras se edita el formulario. */
data class SubtaskDraft(
    val id: String,
    val title: String,
    val completed: Boolean = false
)

/** Datos capturados en el formulario de tarea. */
data class TaskForm(
    val title: String,
    val description: String,
    val subjectId: String?,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val allDay: Boolean,
    val priority: String,
    val subtasks: List<SubtaskDraft>
)
