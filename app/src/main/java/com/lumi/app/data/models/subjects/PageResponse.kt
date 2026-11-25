package com.lumi.app.data.models.subjects

data class PageResponse<T>(
    val items: List<T>,
    val nextCursor: String? = null,
    val hasMore: Boolean,
    val syncToken: String,
    val serverTime: String
)
