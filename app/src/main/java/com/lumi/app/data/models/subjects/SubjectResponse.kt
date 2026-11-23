package com.lumi.app.data.models.subjects

data class SubjectResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val semesterId: String? = null,
    val status: String,
    val version: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)
