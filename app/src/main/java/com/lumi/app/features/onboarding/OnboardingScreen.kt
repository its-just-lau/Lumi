
package com.lumi.app.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.features.onboarding.components.MascotPlaceholder
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.lumi.app.data.local.OnboardingPreferencesRepository
import kotlinx.coroutines.flow.first

private val goals = listOf(
    "Organizar mis actividades",
    "Mejorar mis hábitos de estudio",
    "Aprender nuevos temas",
    "Administrar mis tareas",
    "Dar seguimiento a mi progreso"
)

private data class TourContent(
    val title: String,
    val description: String,
    val emoji: String
)

private val tourPages = listOf(
    TourContent(
        "Aprender",
        "Encuentra materiales, explicaciones, tarjetas y cuestionarios para aprender a tu ritmo.",
        "📚"
    ),
    TourContent(
        "Organizar",
        "Administra tus materias, actividades, horarios y pendientes en un solo lugar.",
        "🗓️"
    ),
    TourContent(
        "Estudiar",
        "Utiliza sesiones de estudio y temporizadores para mantener la concentración.",
        "⏱️"
    ),
    TourContent(
        "Progresar",
        "Consigue experiencia, mantén tu racha y observa cómo avanzas.",
        "⭐"
    )
)

@Composable
fun OnboardingScreen(
    onStartClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current

    val repository = remember {
        OnboardingPreferencesRepository(context)
    }

    var currentStep by remember {
        mutableStateOf(OnboardingStep.WELCOME)
    }

    var selectedGoals by remember {
        mutableStateOf(setOf<String>())
    }

    var dailyGoal by remember {
        mutableIntStateOf(15)
    }

    var tourIndex by remember {
        mutableIntStateOf(0)
    }

    var preferencesLoaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        val savedPreferences = repository.preferences.first()

        selectedGoals = savedPreferences.selectedGoals
        dailyGoal = savedPreferences.dailyGoalMinutes
        tourIndex = savedPreferences.tourIndex.coerceIn(
            0,
            tourPages.lastIndex
        )

        currentStep = runCatching {
            OnboardingStep.valueOf(savedPreferences.currentStep)
        }.getOrDefault(OnboardingStep.WELCOME)

        preferencesLoaded = true
    }

    LaunchedEffect(
        preferencesLoaded,
        currentStep,
        selectedGoals,
        dailyGoal,
        tourIndex
    ) {
        if (preferencesLoaded) {

            repository.saveGoals(selectedGoals)

            repository.saveDailyGoal(dailyGoal)

            repository.saveCurrentStep(
                if (currentStep == OnboardingStep.COMPLETE) {
                    OnboardingStep.TOUR.name
                } else {
                    currentStep.name
                }
            )

            repository.saveTourIndex(tourIndex)

            repository.saveTourCompleted(
                currentStep == OnboardingStep.COMPLETE
            )
        }
    }

    val tourPage = tourPages[tourIndex]

    fun nextStep() {
        currentStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.GOALS
            OnboardingStep.GOALS -> OnboardingStep.DAILY_GOAL
            OnboardingStep.DAILY_GOAL -> OnboardingStep.TOUR
            OnboardingStep.TOUR -> {
                if (tourIndex < tourPages.lastIndex) {
                    tourIndex++
                    OnboardingStep.TOUR
                } else {
                    onRegisterClick()
                    OnboardingStep.COMPLETE
                }
            }
            OnboardingStep.COMPLETE -> OnboardingStep.COMPLETE
        }
    }

    fun previousStep() {
        currentStep = when (currentStep) {
            OnboardingStep.GOALS -> OnboardingStep.WELCOME
            OnboardingStep.DAILY_GOAL -> OnboardingStep.GOALS
            OnboardingStep.TOUR -> {
                if (tourIndex > 0) {
                    tourIndex--
                    OnboardingStep.TOUR
                } else {
                    OnboardingStep.DAILY_GOAL
                }
            }
            else -> OnboardingStep.WELCOME
        }
    }

    fun skipStep() {
        when (currentStep) {
            OnboardingStep.GOALS -> currentStep = OnboardingStep.DAILY_GOAL
            OnboardingStep.DAILY_GOAL -> currentStep = OnboardingStep.TOUR
            OnboardingStep.TOUR -> {
                tourIndex = tourPages.lastIndex
                onRegisterClick()
                currentStep = OnboardingStep.COMPLETE
            }
            else -> Unit
        }
    }

    val progress = when (currentStep) {
        OnboardingStep.WELCOME -> 0.2f
        OnboardingStep.GOALS -> 0.4f
        OnboardingStep.DAILY_GOAL -> 0.6f
        OnboardingStep.TOUR -> 0.7f + (tourIndex * 0.075f)
        OnboardingStep.COMPLETE -> 1f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumiColors.Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lumi",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Dark
        )

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            color = LumiColors.YellowStrong,
            trackColor = LumiColors.WarmGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        MascotPlaceholder(
            modifier = Modifier.height(190.dp),
            message = when (currentStep) {
                OnboardingStep.WELCOME -> "¡Hola! Soy Lumi"
                OnboardingStep.GOALS -> "¡Vamos a definir tus objetivos!"
                OnboardingStep.DAILY_GOAL -> "¡Cada minuto cuenta!"
                OnboardingStep.TOUR -> "¡Te enseño cómo funciona!"
                OnboardingStep.COMPLETE -> "¡Ya casi estamos!"
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (currentStep) {
            OnboardingStep.WELCOME -> {
                Text(
                    text = "¡Aprender puede ser increíble!",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Organiza tus actividades, descubre nuevas formas de aprender y convierte cada sesión de estudio en un logro.",
                    color = LumiColors.Dark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                LumiButton(
                    text = "Comenzar",
                    onClick = {
                        onStartClick()
                        nextStep()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LumiButton(
                    text = "Ya tengo una cuenta",
                    onClick = onLoginClick,
                    backgroundColor = LumiColors.WarmGray
                )
            }

            OnboardingStep.GOALS -> {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Qué quieres lograr con Lumi?",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Selecciona todas las opciones que te interesen.",
                        color = LumiColors.Dark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    goals.forEach { goal ->
                        val selected = goal in selectedGoals

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedGoals = if (selected) {
                                        selectedGoals - goal
                                    } else {
                                        selectedGoals + goal
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) {
                                    LumiColors.Yellow.copy(alpha = 0.45f)
                                } else {
                                    LumiColors.White
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selected,
                                    onCheckedChange = {
                                        selectedGoals = if (it) {
                                            selectedGoals + goal
                                        } else {
                                            selectedGoals - goal
                                        }
                                    }
                                )

                                Text(
                                    text = goal,
                                    color = LumiColors.Dark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                LumiButton(
                    text = "Continuar",
                    onClick = ::nextStep
                )

                TextButton(onClick = ::skipStep) {
                    Text("Omitir", color = LumiColors.Dark)
                }

                TextButton(onClick = ::previousStep) {
                    Text("Atrás", color = LumiColors.Dark)
                }
            }

            OnboardingStep.DAILY_GOAL -> {
                Text(
                    text = "¿Cuánto quieres estudiar al día?",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Puedes cambiar tu objetivo cuando quieras.",
                    color = LumiColors.Dark
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "$dailyGoal min",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Slider(
                    value = dailyGoal.toFloat(),
                    onValueChange = {
                        dailyGoal = it.toInt()
                    },
                    valueRange = 5f..60f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = LumiColors.YellowStrong,
                        activeTrackColor = LumiColors.YellowStrong
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5 min", color = LumiColors.Dark)
                    Text("60 min", color = LumiColors.Dark)
                }

                Spacer(modifier = Modifier.weight(1f))

                LumiButton(
                    text = "Continuar",
                    onClick = ::nextStep
                )

                TextButton(onClick = ::skipStep) {
                    Text("Omitir", color = LumiColors.Dark)
                }

                TextButton(onClick = ::previousStep) {
                    Text("Atrás", color = LumiColors.Dark)
                }
            }

            OnboardingStep.TOUR -> {
                Text(
                    text = "Conoce ${tourPage.title}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = tourPage.emoji,
                    fontSize = 55.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tourPage.description,
                    textAlign = TextAlign.Center,
                    color = LumiColors.Dark
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "${tourIndex + 1} de ${tourPages.size}",
                    color = LumiColors.Dark,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                LumiButton(
                    text = if (tourIndex == tourPages.lastIndex) {
                        "Crear mi cuenta"
                    } else {
                        "Siguiente"
                    },
                    onClick = ::nextStep
                )

                TextButton(onClick = ::skipStep) {
                    Text("Omitir recorrido", color = LumiColors.Dark)
                }

                TextButton(onClick = ::previousStep) {
                    Text("Atrás", color = LumiColors.Dark)
                }
            }

            OnboardingStep.COMPLETE -> {
                Text(
                    text = "¡Todo listo!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            }
        }
    }
}
