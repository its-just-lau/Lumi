package com.lumi.app.data.models.user

data class PreferencesResponse(
    val selectedGoals: List<String>? = null,
    val dailyGoalMinutes: Int? = null
)

data class UserProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val timeZone: String,
    val version: Int? = null
)

data class UpdateProfileRequest(
    val name: String? = null,
    val timeZone: String? = null
)
