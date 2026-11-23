package com.lumi.app.data.models.subjects

data class SemesterResponse(
    val id: String,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val version: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)
