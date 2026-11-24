package com.lumi.app.features.subjects.model

/** Cuerpo para crear o actualizar un semestre (Contrato de API §5). */
data class SemesterInput(
    val id: String,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null
)
