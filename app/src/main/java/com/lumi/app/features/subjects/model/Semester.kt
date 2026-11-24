package com.lumi.app.features.subjects.model

data class Semester(
    val id: String,
    val name: String,
    val startDate: String? = null,
    val subjectCount: Int = 0,
    val isArchived: Boolean = false
)
