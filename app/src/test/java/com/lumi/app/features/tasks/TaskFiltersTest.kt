package com.lumi.app.features.tasks

import com.lumi.app.data.models.tasks.TaskResponse
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class TaskFiltersTest {

    private val now = LocalDateTime.of(2026, 11, 23, 12, 0)

    private fun task(
        id: String,
        dueDate: String? = null,
        dueTime: String? = null,
        status: String = "pending"
    ) = TaskResponse(
        id = id,
        title = id,
        dueDate = dueDate,
        dueTime = dueTime,
        allDay = dueTime == null,
        status = status
    )

    private val tasks = listOf(
        task("ayer", dueDate = "2026-11-22"),
        task("hoy-pasada", dueDate = "2026-11-23", dueTime = "08:00"),
        task("hoy-todo-el-dia", dueDate = "2026-11-23"),
        task("manana", dueDate = "2026-11-24"),
        task("sin-fecha"),
        task("lista", dueDate = "2026-11-20", status = "completed")
    )

    @Test
    fun overdueIncludesPastDatesAndPastTimes() {
        val ids = tasks.applyTaskFilter(TaskFilter.OVERDUE, now).map { it.id }
        assertEquals(listOf("ayer", "hoy-pasada"), ids)
    }

    @Test
    fun todayExcludesCompletedAndOtherDays() {
        val ids = tasks.applyTaskFilter(TaskFilter.TODAY, now).map { it.id }
        assertEquals(listOf("hoy-pasada", "hoy-todo-el-dia"), ids)
    }

    @Test
    fun upcomingOnlyFutureDates() {
        val ids = tasks.applyTaskFilter(TaskFilter.UPCOMING, now).map { it.id }
        assertEquals(listOf("manana"), ids)
    }

    @Test
    fun completedFilter() {
        val ids = tasks.applyTaskFilter(TaskFilter.COMPLETED, now).map { it.id }
        assertEquals(listOf("lista"), ids)
    }

    @Test
    fun allPutsPendingFirstAndNoDateLast() {
        val ids = tasks.applyTaskFilter(TaskFilter.ALL, now).map { it.id }
        assertEquals(
            listOf("ayer", "hoy-pasada", "hoy-todo-el-dia", "manana", "sin-fecha", "lista"),
            ids
        )
    }
}
