package com.lumi.app.core.navigation

sealed class Screen(val route: String) {

    data object Welcome : Screen("welcome")

    data object Login : Screen("login")

    data object Register : Screen("register")

    data object Home : Screen("home")

    data object Subjects : Screen("subjects")

    data object Study : Screen("study")

    data object Profile : Screen("profile")

    data object Store : Screen("store")

    data object Tasks : Screen("tasks")

    data object Onboarding : Screen("onboarding")

    data object OnboardingComplete : Screen("onboarding_complete")
}
