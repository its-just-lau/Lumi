
package com.lumi.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import com.lumi.app.features.auth.WelcomeScreen
import com.lumi.app.features.auth.LoginScreen
import com.lumi.app.features.auth.RegisterScreen
import com.lumi.app.features.home.HomeScreen
import androidx.compose.material3.Text


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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
                    // Próximamente conectaremos el registro con nuestra API.
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) {
                            inclusive = true
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
            // Aquí construiremos SubjectsScreen
        }

        composable(Screen.Study.route) {
            // Aquí construiremos StudyScreen
        }

        composable(Screen.Profile.route) {
            // Aquí construiremos ProfileScreen
        }

        composable(Screen.Store.route) {
            Text("Tienda de Lumi 🛍️")
        }
    }
}
