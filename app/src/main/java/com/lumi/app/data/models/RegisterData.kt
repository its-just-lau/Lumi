package com.lumi.app.data.models

data class RegisterData(
    val name: String,
    val email: String,
    val password: String,
    // Zona horaria IANA del dispositivo, por ejemplo "America/Monterrey".
    val timeZone: String,
    val selectedGoals: Set<String>,
    val dailyGoalMinutes: Int
)
