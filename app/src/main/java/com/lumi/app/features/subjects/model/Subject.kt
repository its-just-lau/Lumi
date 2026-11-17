package com.lumi.app.features.subjects.model

data class Subject(
    val id: Int,
    val name: String,
    val semesterId: Int? = null,
    val teacher: String? = null,
    val description: String? = null,
    val color: Long,
    val progress: Int = 0,
    val isDraft: Boolean = false
)
