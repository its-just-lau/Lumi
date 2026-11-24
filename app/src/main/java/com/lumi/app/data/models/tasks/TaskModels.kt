package com.lumi.app.data.models.tasks

/** Modelos de tareas y subtareas según el Contrato de API (§6). */

data class SubtaskResponse(
    val id: String,
    val title: String,
    val completed: Boolean = false,
    val version: Int = 1,
    val deletedAt: String? = null
)

data class TaskResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    val subjectId: String? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val allDay: Boolean = false,
    val timeZone: String? = null,
    val priority: String = "medium",
    val status: String = "pending",
    val subtasks: List<SubtaskResponse>? = null,
    val completedAt: String? = null,
    val rewardedAt: String? = null,
    val version: Int = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)

data class SubtaskInput(
    val id: String,
    val title: String,
    val completed: Boolean = false
)

data class TaskInput(
    val id: String,
    val title: String,
    val description: String? = null,
    val subjectId: String? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val allDay: Boolean = false,
    val timeZone: String? = null,
    val priority: String = "medium",
    val subtasks: List<SubtaskInput> = emptyList()
)

data class CompleteTaskRequest(
    val completedAt: String,
    val timeZone: String
)

data class WalletResponse(
    val xp: Int = 0,
    val gems: Int = 0
)

data class StreakResponse(
    val current: Int = 0,
    val longest: Int = 0
)

data class RewardResult(
    val status: String,
    val reason: String? = null,
    val xpAwarded: Int = 0,
    val gemsAwarded: Int = 0,
    val wallet: WalletResponse? = null,
    val streak: StreakResponse? = null
)

data class TaskCompletionResponse(
    val task: TaskResponse? = null,
    val rewardResult: RewardResult? = null
)
