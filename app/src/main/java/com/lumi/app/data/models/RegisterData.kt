package com.lumi.app.data.models

data class RegisterData(
    val name: String,
    val email: String,
    val password: String,
    val selectedGoals: Set<String>,
    val dailyGoalMinutes: Int
)
