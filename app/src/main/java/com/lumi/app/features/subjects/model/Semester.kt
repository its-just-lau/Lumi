package com.lumi.app.features.subjects.model

data class Semester(
    val id: Int,
    val name: String,
    val subjectCount: Int = 0,
    val isArchived: Boolean = false
)
