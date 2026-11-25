package com.lumi.app.features.subjects.mapper

import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.features.subjects.model.Semester
import com.lumi.app.features.subjects.model.Subject

private const val DEFAULT_SUBJECT_COLOR = 0xFFFFE6A3L

/** Convierte la respuesta del API en el modelo que usa la interfaz. */
fun SemesterResponse.toSemester(): Semester {
    return Semester(
        id = id,
        name = name,
        startDate = startDate
    )
}

fun SubjectResponse.toSubject(): Subject {
    return Subject(
        id = id,
        name = name,
        semesterId = semesterId,
        description = description,
        color = color.toColorLongOrDefault()
    )
}

/** Acepta "#RRGGBB" o "#AARRGGBB"; si no es válido usa el color por defecto. */
fun String?.toColorLongOrDefault(): Long {
    val hex = this?.removePrefix("#") ?: return DEFAULT_SUBJECT_COLOR

    val value = when (hex.length) {
        6 -> "FF$hex".toLongOrNull(16)
        8 -> hex.toLongOrNull(16)
        else -> null
    }

    return value ?: DEFAULT_SUBJECT_COLOR
}
