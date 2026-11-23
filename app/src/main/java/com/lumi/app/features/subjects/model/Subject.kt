package com.lumi.app.features.subjects.model

data class Subject(
    val id: String,
    val name: String,
    val semesterId: String? = null,
    val teacher: String? = null,
    val description: String? = null,
    val color: Long = 0xFFFFE6A3,
    val progress: Int = 0,
    val isDraft: Boolean = false
)
