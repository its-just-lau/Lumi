package com.lumi.app.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lumi.app.LumiApp
import com.lumi.app.core.storage.SessionState
import com.lumi.app.data.local.OnboardingPreferencesRepository
import com.lumi.app.data.models.LoginData
import com.lumi.app.data.models.RegisterData
import com.lumi.app.features.auth.AuthUiState
import com.lumi.app.features.auth.AuthViewModel
import com.lumi.app.features.auth.AuthViewModelFactory
import com.lumi.app.features.auth.ForgotPasswordScreen
import com.lumi.app.features.auth.PasswordRecoveryViewModel
import com.lumi.app.features.auth.PasswordRecoveryViewModelFactory
import com.lumi.app.features.auth.ResetPasswordScreen
import com.lumi.app.features.auth.LoginScreen
import com.lumi.app.features.auth.RegisterScreen
import com.lumi.app.features.auth.WelcomeScreen
import com.lumi.app.features.home.HomeScreen
import com.lumi.app.features.onboarding.OnboardingCompleteScreen
import com.lumi.app.features.onboarding.OnboardingScreen
import com.lumi.app.features.profile.ProfileScreen
import com.lumi.app.features.settings.EditProfileScreen
import com.lumi.app.features.settings.NotificationsScreen
import com.lumi.app.features.settings.PreferencesScreen
import com.lumi.app.features.settings.SettingsScreen
import com.lumi.app.features.store.StoreScreen
import com.lumi.app.features.study.StudyScreen
import com.lumi.app.features.subjects.SubjectsScreen
import com.lumi.app.features.tasks.TasksScreen
import com.lumi.app.ui.theme.LumiColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val container = LumiApp.container
    val sessionManager = container.sessionManager
    val sessionState by sessionManager.state.collectAsState()

    val onboardingRepository = remember {
        OnboardingPreferencesRepository(context)
    }

    val coroutineScope = rememberCoroutineScope()

    // Se decide una sola vez al abrir la app: con sesión guardada entra a Home,
    // sin sesión empieza en Bienvenida.
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        sessionManager.restore()

        startDestination =
            if (sessionManager.state.value is SessionState.Authenticated) {
                Screen.Home.route
            } else {
                Screen.Welcome.route
            }
    }

    // Si el servidor rechaza el refresh token, la sesión expira y se vuelve a Login.
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Expired && startDestination != null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    val start = startDestination

    if (start == null) {
        // Fracción de segundo mientras se recupera la sesión guardada.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
        )
        return
    }

    NavHost(
        navController = navController,
        startDestination = start

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
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(container.authRepository)
            )
            val authState by authViewModel.uiState.collectAsState()

            LaunchedEffect(authState) {
                if (authState is AuthUiState.Success) {
                    authViewModel.resetState()

                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }

            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(
                        LoginData(
                            email = email,
                            password = password
                        )
                    )
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onGoogleIdToken = { idToken ->
                    authViewModel.loginWithGoogle(idToken)
                },
                isLoading = authState is AuthUiState.Loading,
                errorMessage = (authState as? AuthUiState.Error)?.message
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(container.authRepository)
            )
            val authState by authViewModel.uiState.collectAsState()

            LaunchedEffect(authState) {
                if (authState is AuthUiState.Success) {
                    authViewModel.resetState()

                    navController.navigate(Screen.OnboardingComplete.route) {
                        popUpTo(Screen.Welcome.route) {
                            inclusive = true
                        }
                    }
                }
            }

            RegisterScreen(
                onRegisterClick = { name, email, password ->

                    coroutineScope.launch {

                        val preferences = onboardingRepository.preferences.first()

                        authViewModel.register(
                            RegisterData(
                                name = name,
                                email = email,
                                password = password,
                                timeZone = ZoneId.systemDefault().id,
                                selectedGoals = preferences.selectedGoals,
                                dailyGoalMinutes = preferences.dailyGoalMinutes
                            )
                        )
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = authState is AuthUiState.Loading,
                serverErrorMessage = (authState as? AuthUiState.Error)?.message
            )
        }

        composable(Screen.ForgotPassword.route) {
            val recoveryViewModel: PasswordRecoveryViewModel = viewModel(
                factory = PasswordRecoveryViewModelFactory(container.authRepository)
            )
            val forgotState by recoveryViewModel.forgotState.collectAsState()

            ForgotPasswordScreen(
                onSendClick = { email ->
                    recoveryViewModel.forgotPassword(email)
                },
                onHaveCodeClick = {
                    navController.navigate(Screen.ResetPassword.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = forgotState is AuthUiState.Loading,
                errorMessage = (forgotState as? AuthUiState.Error)?.message,
                successMessage = (forgotState as? AuthUiState.Success)?.message
            )
        }

        composable(Screen.ResetPassword.route) {
            val recoveryViewModel: PasswordRecoveryViewModel = viewModel(
                factory = PasswordRecoveryViewModelFactory(container.authRepository)
            )
            val resetState by recoveryViewModel.resetState.collectAsState()

            LaunchedEffect(resetState) {
                if (resetState is AuthUiState.Success) {
                    recoveryViewModel.clearResetState()

                    // Contraseña cambiada: vuelve a Login para entrar con la nueva.
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            }

            ResetPasswordScreen(
                onResetClick = { token, newPassword ->
                    recoveryViewModel.resetPassword(token, newPassword)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = resetState is AuthUiState.Loading,
                serverErrorMessage = (resetState as? AuthUiState.Error)?.message
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

        composable(Screen.Settings.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(container.authRepository)
            )

            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onLogoutClick = {
                    authViewModel.logout {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Preferences.route) {
            PreferencesScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
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
