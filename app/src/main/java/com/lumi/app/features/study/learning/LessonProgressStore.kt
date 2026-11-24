package com.lumi.app.features.study.learning

import android.content.Context

/**
 * Guarda localmente el porcentaje de avance de cada lección guiada.
 *
 * El contrato v1.3 aún no tiene un endpoint de progreso parcial; cuando exista
 * (PUT /lessons/{id}/progress) este almacén se sincroniza con el servidor.
 */
class LessonProgressStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("lumi_lesson_progress", Context.MODE_PRIVATE)

    fun get(lessonId: String): Int = prefs.getInt(lessonId, 0)

    /** Solo guarda si el avance es mayor al que ya había. */
    fun save(lessonId: String, percent: Int) {
        val value = percent.coerceIn(0, 100)
        if (value > get(lessonId)) {
            prefs.edit().putInt(lessonId, value).apply()
        }
    }
}
