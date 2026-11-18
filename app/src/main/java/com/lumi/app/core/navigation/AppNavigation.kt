package com.lumi.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lumi.app.data.local.OnboardingPreferencesRepository
import com.lumi.app.data.models.RegisterData
import com.lumi.app.features.auth.LoginScreen
import com.lumi.app.features.auth.RegisterScreen
import com.lumi.app.features.auth.WelcomeScreen
import com.lumi.app.features.home.HomeScreen
import com.lumi.app.features.onboarding.OnboardingCompleteScreen
import com.lumi.app.features.onboarding.OnboardingScreen
import com.lumi.app.features.profile.ProfileScreen
import com.lumi.app.features.store.StoreScreen
import com.lumi.app.features.study.StudyScreen
import com.lumi.app.features.subjects.SubjectsScreen
import com.lumi.app.features.tasks.TasksScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val onboardingRepository = remember {
        OnboardingPreferencesRepository(context)
    }

    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route

    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onStartClick = {},
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    // Próximamente conectaremos la autenticación.
                    navController.navigate(Screen.Home.route)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = { name, email, password ->

                    coroutineScope.launch {

                        val preferences = onboardingRepository.preferences.first()

                        val registerData = RegisterData(
                            name = name,
                            email = email,
                            password = password,
                            selectedGoals = preferences.selectedGoals,
                            dailyGoalMinutes = preferences.dailyGoalMinutes
                        )

                        // Por ahora solamente preparamos los datos.
                        // Posteriormente registerData se enviará a la API.

                        navController.navigate(Screen.OnboardingComplete.route) {
                            popUpTo(Screen.Welcome.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.OnboardingComplete.route) {
            OnboardingCompleteScreen(
                onContinueClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OnboardingComplete.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

        composable(Screen.Subjects.route) {
            SubjectsScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Study.route) {
            StudyScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Store.route) {
            StoreScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
