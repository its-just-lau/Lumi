package com.lumi.app.core.navigation

sealed class Screen(val route: String) {

    data object Welcome : Screen("welcome")

    data object Login : Screen("login")

    data object Register : Screen("register")

    data object ForgotPassword : Screen("forgot_password")

    data object ResetPassword : Screen("reset_password")

    data object Home : Screen("home")

    data object Subjects : Screen("subjects")

    data object Study : Screen("study")

    data object Profile : Screen("profile")

    data object Store : Screen("store")

    data object Tasks : Screen("tasks")

    data object Onboarding : Screen("onboarding")

    data object OnboardingComplete : Screen("onboarding_complete")

    data object Settings : Screen("settings")

    data object EditProfile : Screen("edit-profile")

    data object Preferences : Screen("preferences")

    data object Notifications : Screen("notifications")
}
