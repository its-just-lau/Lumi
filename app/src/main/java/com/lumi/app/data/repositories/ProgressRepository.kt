package com.lumi.app.data.repositories

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.lumi.app.core.network.ProgressService
import com.lumi.app.data.models.tasks.StreakResponse
import com.lumi.app.data.models.tasks.WalletResponse
import kotlinx.coroutines.CancellationException

/** Actividad de la semana: XP y minutos por día (hasta 7 valores, el último es hoy). */
data class WeeklyProgress(
    val xp: Int = 0,
    val dailyMinutes: List<Int> = emptyList()
)

/** Totales de estudio desde que se creó la cuenta. */
data class ProgressOverview(
    val studyMinutes: Int = 0,
    val completedLessons: Int = 0
)

class ProgressRepository(
    private val progressService: ProgressService
) {

    suspend fun getWallet(): Result<WalletResponse> = safeCall { progressService.getWallet() }

    suspend fun getStreak(): Result<StreakResponse> = safeCall { progressService.getStreak() }

    suspend fun getWeekly(): Result<WeeklyProgress> = safeCall {
        parseWeekly(progressService.getWeekly())
    }

    suspend fun getWeeklyXp(): Result<Int> = safeCall {
        parseWeekly(progressService.getWeekly()).xp
    }

    suspend fun getOverview(): Result<ProgressOverview> = safeCall {
        parseOverview(progressService.getProgress())
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}

// El contrato aún no define los campos de /progress ni de /progress/weekly.
// Se leen de forma tolerante; pendiente que el backend fije los nombres.

internal fun parseWeekly(json: JsonObject): WeeklyProgress {
    val xp = json.firstInt("weeklyXp", "xp", "xpEarned", "totalXp")

    val days = listOf("days", "items")
        .mapNotNull { json.get(it) }
        .firstOrNull { it.isJsonArray }
        ?.asJsonArray
        ?.mapNotNull { it.takeIf { e -> e.isJsonObject }?.asJsonObject }
        .orEmpty()

    val minutes = days.map { it.firstInt("minutes", "studyMinutes", "studiedMinutes") }

    val xpFromDays = days.sumOf { it.firstInt("xp") ?: 0 }

    return WeeklyProgress(
        xp = xp ?: xpFromDays,
        dailyMinutes = minutes.map { it ?: 0 }.takeLast(7)
    )
}

internal fun parseOverview(json: JsonObject): ProgressOverview {
    val minutes = json.firstInt("totalStudyMinutes", "studyMinutes", "totalMinutes")
        ?: json.firstInt("totalStudyHours", "studyHours")?.times(60)
        ?: 0

    return ProgressOverview(
        studyMinutes = minutes,
        completedLessons = json.firstInt("completedLessons", "lessonsCompleted") ?: 0
    )
}

private fun JsonObject.firstInt(vararg keys: String): Int? {
    for (key in keys) {
        val value: JsonElement? = get(key)
        if (value != null && value.isJsonPrimitive && value.asJsonPrimitive.isNumber) {
            return value.asInt
        }
    }
    return null
}
