package com.lumi.app.features.tasks

import com.lumi.app.data.models.tasks.TaskResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object TaskFilter {
    const val ALL = "all"
    const val TODAY = "today"
    const val UPCOMING = "upcoming"
    const val OVERDUE = "overdue"
    const val COMPLETED = "completed"

    val all = listOf(ALL, TODAY, UPCOMING, OVERDUE, COMPLETED)
}

fun TaskResponse.isCompleted(): Boolean = status == "completed"

fun TaskResponse.dueDateOrNull(): LocalDate? {
    return try {
        dueDate?.let { LocalDate.parse(it) }
    } catch (exception: Exception) {
        null
    }
}

fun TaskResponse.dueTimeOrNull(): LocalTime? {
    return try {
        if (allDay) null else dueTime?.let { LocalTime.parse(it) }
    } catch (exception: Exception) {
        null
    }
}

/** Vencida: sigue pendiente y su fecha (o fecha y hora) ya pasó. */
fun TaskResponse.isOverdue(now: LocalDateTime): Boolean {
    if (isCompleted()) return false

    val date = dueDateOrNull() ?: return false
    val time = dueTimeOrNull()

    return if (time == null) {
        date.isBefore(now.toLocalDate())
    } else {
        LocalDateTime.of(date, time).isBefore(now)
    }
}

fun List<TaskResponse>.applyTaskFilter(
    selected: String,
    now: LocalDateTime
): List<TaskResponse> {
    val today = now.toLocalDate()

    val filtered = when (selected) {
        TaskFilter.TODAY -> filter { !it.isCompleted() && it.dueDateOrNull() == today }
        TaskFilter.UPCOMING -> filter {
            !it.isCompleted() && it.dueDateOrNull()?.isAfter(today) == true
        }
        TaskFilter.OVERDUE -> filter { it.isOverdue(now) }
        TaskFilter.COMPLETED -> filter { it.isCompleted() }
        else -> this
    }

    // Pendientes primero, luego por fecha y hora; sin fecha al final.
    return filtered.sortedWith(
        compareBy<TaskResponse> { it.isCompleted() }
            .thenBy { it.dueDateOrNull() ?: LocalDate.MAX }
            .thenBy { it.dueTimeOrNull() ?: LocalTime.MAX }
            .thenBy { it.title.lowercase() }
    )
}
