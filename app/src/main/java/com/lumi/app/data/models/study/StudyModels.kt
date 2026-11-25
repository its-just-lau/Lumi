package com.lumi.app.data.models.study

import com.lumi.app.data.models.tasks.RewardResult

/** Sesión de estudio a registrar (Contrato de API §7). */
data class StudySessionInput(
    val id: String,
    val subjectId: String? = null,
    val mode: String,
    val plannedMinutes: Int? = null,
    val pausedSeconds: Int = 0,
    val startedAt: String,
    val endedAt: String,
    val timeZone: String
)

data class StudySessionResponse(
    val id: String,
    val durationMinutes: Int? = null,
    val status: String? = null,
    val version: Int? = null
)

data class StudySessionResult(
    val session: StudySessionResponse? = null,
    val rewardResult: RewardResult? = null
)
