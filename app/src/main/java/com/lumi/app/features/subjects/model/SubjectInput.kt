package com.lumi.app.features.subjects.model

data class SubjectInput(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val semesterId: String? = null,
    val status: String = "active"
)