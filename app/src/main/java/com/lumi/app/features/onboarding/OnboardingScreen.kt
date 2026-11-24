
package com.lumi.app.features.onboarding

import com.lumi.app.core.components.LumiEmoji
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
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText
import androidx.compose.ui.platform.LocalConfiguration

private val goals: List<String> get() = listOf(
    AppText.get(R.string.organizar_mis_actividades),
    AppText.get(R.string.mejorar_mis_habitos_de_estudio),
    AppText.get(R.string.aprender_nuevos_temas),
    AppText.get(R.string.administrar_mis_tareas),
    AppText.get(R.string.dar_seguimiento_a_mi_progreso)
)

private data class TourContent(
    val title: String,
    val description: String,
    val emoji: String
)

private val tourPages: List<TourContent> get() = listOf(
    TourContent(
        AppText.get(R.string.aprender),
        AppText.get(R.string.encuentra_materiales_explicaciones_tarjetas_y),
        "📚"
    ),
    TourContent(
        AppText.get(R.string.organizar),
        AppText.get(R.string.administra_tus_materias_actividades_horarios),
        "🗓️"
    ),
    TourContent(
        AppText.get(R.string.estudiar),
        AppText.get(R.string.utiliza_sesiones_de_estudio_y),
        "⏱️"
    ),
    TourContent(
        AppText.get(R.string.progresar),
        AppText.get(R.string.consigue_experiencia_manten_tu_racha),
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

    // Deja ver la pantalla final unos segundos antes de ir al registro.
    LaunchedEffect(currentStep) {
        if (currentStep == OnboardingStep.COMPLETE) {
            kotlinx.coroutines.delay(COMPLETE_STEP_MILLIS)
            onRegisterClick()
        }
    }

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
            text = stringResource(R.string.lumi),
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Yellow
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

        val screenHeight = LocalConfiguration.current.screenHeightDp
        val hasAnimation = currentStep == OnboardingStep.WELCOME ||
                currentStep == OnboardingStep.GOALS ||
                currentStep == OnboardingStep.DAILY_GOAL ||
                currentStep == OnboardingStep.COMPLETE

        MascotPlaceholder(
            modifier = if (hasAnimation) Modifier else Modifier.height(190.dp),
            animationHeight = if (currentStep == OnboardingStep.WELCOME) {
                (screenHeight * 0.45f).dp
            } else {
                210.dp
            },
            animationRes = when (currentStep) {
                OnboardingStep.WELCOME -> R.raw.lumi_saludo
                OnboardingStep.GOALS, OnboardingStep.DAILY_GOAL -> R.raw.lumi_pensando
                OnboardingStep.COMPLETE -> R.raw.lumi_todo_listo
                else -> null
            },
            message = when (currentStep) {
                OnboardingStep.WELCOME -> stringResource(R.string.hola_soy_lumi)
                OnboardingStep.GOALS -> stringResource(R.string.vamos_a_definir_tus_objetivos)
                OnboardingStep.DAILY_GOAL -> stringResource(R.string.cada_minuto_cuenta)
                OnboardingStep.TOUR -> stringResource(R.string.te_enseno_como_funciona)
                OnboardingStep.COMPLETE -> stringResource(R.string.ya_casi_estamos)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (currentStep) {
            OnboardingStep.WELCOME -> {
                Text(
                    text = stringResource(R.string.aprender_puede_ser_increible),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.organiza_tus_actividades_descubre_nuevas),
                    color = LumiColors.Dark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                LumiButton(
                    text = stringResource(R.string.comenzar),
                    onClick = {
                        onStartClick()
                        nextStep()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LumiButton(
                    text = stringResource(R.string.ya_tengo_una_cuenta),
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
                        text = stringResource(R.string.que_quieres_lograr_con_lumi),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.selecciona_todas_las_opciones_que),
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
                    text = stringResource(R.string.continuar),
                    onClick = ::nextStep
                )

                TextButton(onClick = ::skipStep) {
                    Text(stringResource(R.string.omitir), color = LumiColors.Dark)
                }

                TextButton(onClick = ::previousStep) {
                    Text(stringResource(R.string.atras), color = LumiColors.Dark)
                }
            }

            OnboardingStep.DAILY_GOAL -> {
                Text(
                    text = stringResource(R.string.cuanto_quieres_estudiar_al_dia),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.puedes_cambiar_tu_objetivo_cuando),
                    color = LumiColors.Dark
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.min, dailyGoal),
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
                    Text(stringResource(R.string.s_5_min), color = LumiColors.Dark)
                    Text(stringResource(R.string.s_60_min), color = LumiColors.Dark)
                }

                Spacer(modifier = Modifier.weight(1f))

                LumiButton(
                    text = stringResource(R.string.continuar),
                    onClick = ::nextStep
                )

                TextButton(onClick = ::skipStep) {
                    Text(stringResource(R.string.omitir), color = LumiColors.Dark)
                }

                TextButton(onClick = ::previousStep) {
                    Text(stringResource(R.string.atras), color = LumiColors.Dark)
                }
            }

            OnboardingStep.TOUR -> {
                Text(
                    text = stringResource(R.string.conoce, tourPage.title),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Spacer(modifier = Modifier.height(20.dp))

                LumiEmoji(tourPage.emoji, size = 60.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tourPage.description,
                    textAlign = TextAlign.Center,
                    color = LumiColors.Dark
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.de, tourIndex + 1, tourPages.size),
                    color = LumiColors.Dark,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                LumiButton(
                    text = if (tourIndex == tourPages.lastIndex) {
                        AppText.get(R.string.crear_mi_cuenta)
                    } else {
                        AppText.get(R.string.siguiente)
                    },
                    onClick = ::nextStep
                )

                TextButton(onClick = ::skipStep) {
                    Text(stringResource(R.string.omitir_recorrido), color = LumiColors.Dark)
                }

                TextButton(onClick = ::previousStep) {
                    Text(stringResource(R.string.atras), color = LumiColors.Dark)
                }
            }

            OnboardingStep.COMPLETE -> {
                Text(
                    text = stringResource(R.string.todo_listo),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            }
        }
    }
}

private const val COMPLETE_STEP_MILLIS = 2000L
