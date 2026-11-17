package com.lumi.app.features.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.ui.theme.LumiColors
import kotlinx.coroutines.delay

@Composable
fun StudyScreen(
    onNavigate: (String) -> Unit
) {
    // DATOS PROVISIONALES
    val gems = 1439
    val streakDays = 20
    val weeklyXp = 350

    val subjects = listOf(
        "Sin materia",
        "Cálculo II",
        "Programación móvil",
        "Bases de datos",
        "Ingeniería de software"
    )

    var selectedSubject by remember {
        mutableStateOf("Cálculo II")
    }

    var subjectMenuExpanded by remember {
        mutableStateOf(false)
    }

    // CONFIGURACIÓN POMODORO
    var studyMinutes by remember { mutableIntStateOf(25) }
    var shortBreakMinutes by remember { mutableIntStateOf(5) }
    var longBreakMinutes by remember { mutableIntStateOf(15) }
    var cyclesBeforeLongBreak by remember { mutableIntStateOf(4) }

    var showSettings by remember { mutableStateOf(false) }

    // TEMPORIZADOR
    var isPomodoro by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    val totalSeconds = studyMinutes * 60

    val timerProgress = if (isPomodoro) {
        if (totalSeconds > 0) {
            remainingSeconds.toFloat() / totalSeconds
        } else 0f
    } else {
        1f
    }

    // RELOJ DEL TEMPORIZADOR
    LaunchedEffect(isRunning, isPomodoro) {
        while (isRunning) {
            delay(1000)

            if (isPomodoro) {
                if (remainingSeconds > 0) {
                    remainingSeconds--
                } else {
                    isRunning = false
                }
            } else {
                elapsedSeconds++
            }
        }
    }

    val displayedSeconds = if (isPomodoro) {
        remainingSeconds
    } else {
        elapsedSeconds
    }

    val minutesText = (displayedSeconds / 60)
        .toString().padStart(2, '0')

    val secondsText = (displayedSeconds % 60)
        .toString().padStart(2, '0')

    Scaffold(
        containerColor = LumiColors.Cream,
        bottomBar = {
            LumiBottomBar(
                selectedRoute = "study",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ENCABEZADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Estudiar",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💎 $gems",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark
                    )

                    Text(
                        text = "🔥 $streakDays",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark
                    )

                    Text(
                        text = "⭐ $weeklyXp XP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark
                    )
                }
            }

            // TEMPORIZADOR PRINCIPAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LumiColors.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // MENSAJE DE LUMI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🦎",
                            fontSize = 38.sp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(LumiColors.Cream)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (isRunning) {
                                    "¡Muy bien! Sigue concentrándote. 💛"
                                } else {
                                    "¡Vamos a aprovechar el tiempo de hoy!"
                                },
                                fontSize = 13.sp,
                                color = LumiColors.Dark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SELECTOR DE MATERIA
                    Box {
                        Surface(
                            onClick = {
                                subjectMenuExpanded = true
                            },
                            shape = CircleShape,
                            color = LumiColors.Cream
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 18.dp,
                                    vertical = 11.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedSubject,
                                    color = LumiColors.Dark,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "  ⌄",
                                    color = LumiColors.Dark
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = subjectMenuExpanded,
                            onDismissRequest = {
                                subjectMenuExpanded = false
                            }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject) },
                                    onClick = {
                                        selectedSubject = subject
                                        subjectMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // CÍRCULO DEL TEMPORIZADOR
                    Box(
                        modifier = Modifier.size(194.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { timerProgress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = LumiColors.YellowStrong,
                            trackColor = LumiColors.WarmGray,
                            strokeWidth = 11.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$minutesText:$secondsText",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark
                            )

                            Text(
                                text = if (isPomodoro) {
                                    "Enfoque · Sesión"
                                } else {
                                    "Tiempo libre"
                                },
                                fontSize = 13.sp,
                                color = LumiColors.Dark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // CAMBIO DE MODO
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(LumiColors.Cream)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isPomodoro) LumiColors.Yellow
                                    else Color.Transparent
                                )
                                .clickable {
                                    isPomodoro = true
                                    isRunning = false
                                    remainingSeconds = studyMinutes * 60
                                    elapsedSeconds = 0
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pomodoro ${studyMinutes}/${shortBreakMinutes}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LumiColors.Dark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(
                                    if (!isPomodoro) LumiColors.Yellow
                                    else Color.Transparent
                                )
                                .clickable {
                                    isPomodoro = false
                                    isRunning = false
                                    elapsedSeconds = 0
                                    remainingSeconds = studyMinutes * 60
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tiempo libre",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LumiColors.Dark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // BOTONES DEL TEMPORIZADOR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isPomodoro &&
                                    remainingSeconds == 0
                                ) {
                                    remainingSeconds = studyMinutes * 60
                                }

                                isRunning = !isRunning
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LumiColors.Dark,
                                contentColor = LumiColors.White
                            )
                        ) {
                            Icon(
                                imageVector = if (isRunning) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = if (isRunning) "Pausar sesión"
                                else "Iniciar sesión",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                isRunning = false
                                elapsedSeconds = 0
                                remainingSeconds = studyMinutes * 60
                            },
                            modifier = Modifier
                                .size(54.dp)
                                .background(
                                    LumiColors.Cream,
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reiniciar",
                                tint = LumiColors.Dark
                            )
                        }

                        IconButton(
                            onClick = {
                                isRunning = false
                                showSettings = true
                            },
                            modifier = Modifier
                                .size(54.dp)
                                .background(
                                    LumiColors.Cream,
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configurar Pomodoro",
                                tint = LumiColors.Dark
                            )
                        }
                    }
                }
            }

            // LECCIÓN RECOMENDADA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigate("study")
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LumiColors.Dark
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "LECCIÓN RECOMENDADA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Yellow
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Derivadas: regla de la cadena",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.White
                        )

                        Text(
                            text = "12 min · +30 XP",
                            fontSize = 13.sp,
                            color = LumiColors.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LumiColors.Yellow)
                            .clickable {
                                onNavigate("study")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Abrir lección",
                            tint = LumiColors.Dark
                        )
                    }
                }
            }

            // LECCIONES GUIADAS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Lecciones guiadas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                GuidedLessonCard(
                    title = "Regla de la cadena",
                    subject = "Cálculo II",
                    duration = "12 min",
                    xp = "+30 XP",
                    emoji = "📘"
                )

                GuidedLessonCard(
                    title = "Integrales básicas",
                    subject = "Cálculo II",
                    duration = "15 min",
                    xp = "+35 XP",
                    emoji = "✏️"
                )

                GuidedLessonCard(
                    title = "Introducción a Kotlin",
                    subject = "Programación móvil",
                    duration = "10 min",
                    xp = "+25 XP",
                    emoji = "📱"
                )
            }

            // ACCESOS RÁPIDOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStudyCard(
                    emoji = "▤",
                    title = "Tarjetas",
                    modifier = Modifier.weight(1f)
                )

                QuickStudyCard(
                    emoji = "❔",
                    title = "Cuestionario",
                    modifier = Modifier.weight(1f)
                )

                QuickStudyCard(
                    emoji = "📄",
                    title = "Materiales",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    // CONFIGURACIÓN POMODORO
    if (showSettings) {
        PomodoroSettingsDialog(
            studyMinutes = studyMinutes,
            shortBreakMinutes = shortBreakMinutes,
            longBreakMinutes = longBreakMinutes,
            cycles = cyclesBeforeLongBreak,
            onDismiss = {
                showSettings = false
            },
            onSave = { study, shortBreak, longBreak, cycles ->
                studyMinutes = study
                shortBreakMinutes = shortBreak
                longBreakMinutes = longBreak
                cyclesBeforeLongBreak = cycles

                remainingSeconds = study * 60
                elapsedSeconds = 0
                isRunning = false

                showSettings = false
            }
        )
    }
}

@Composable
private fun GuidedLessonCard(
    title: String,
    subject: String,
    duration: String,
    xp: String,
    emoji: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LumiColors.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LumiColors.Cream),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 23.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
                Text(
                    text = subject,
                    fontSize = 12.sp,
                    color = LumiColors.Dark.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$duration · $xp",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LumiColors.YellowStrong
                )
            }

            Text(
                text = "›",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = LumiColors.Dark
            )
        }
    }
}

@Composable
private fun QuickStudyCard(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(105.dp)
            .clickable {
                // Acceso preparado para su próxima implementación
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = LumiColors.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 25.sp
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = LumiColors.Dark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PomodoroSettingsDialog(
    studyMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    cycles: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var study by remember(studyMinutes) {
        mutableStateOf(studyMinutes.toString())
    }

    var shortBreak by remember(shortBreakMinutes) {
        mutableStateOf(shortBreakMinutes.toString())
    }

    var longBreak by remember(longBreakMinutes) {
        mutableStateOf(longBreakMinutes.toString())
    }

    var cycleCount by remember(cycles) {
        mutableStateOf(cycles.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LumiColors.White,
        title = {
            Text(
                text = "Configurar Pomodoro",
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Personaliza tus tiempos en minutos.",
                    color = LumiColors.Dark,
                    fontSize = 13.sp
                )

                SettingsNumberField(
                    label = "Tiempo de estudio",
                    value = study,
                    onValueChange = { study = it }
                )

                SettingsNumberField(
                    label = "Descanso corto",
                    value = shortBreak,
                    onValueChange = { shortBreak = it }
                )

                SettingsNumberField(
                    label = "Descanso largo",
                    value = longBreak,
                    onValueChange = { longBreak = it }
                )

                SettingsNumberField(
                    label = "Ciclos antes del descanso largo",
                    value = cycleCount,
                    onValueChange = { cycleCount = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val studyValue = study.toIntOrNull()
                    val shortValue = shortBreak.toIntOrNull()
                    val longValue = longBreak.toIntOrNull()
                    val cycleValue = cycleCount.toIntOrNull()

                    if (
                        studyValue != null &&
                        shortValue != null &&
                        longValue != null &&
                        cycleValue != null &&
                        studyValue in 1..180 &&
                        shortValue in 1..60 &&
                        longValue in 1..90 &&
                        cycleValue in 1..10
                    ) {
                        onSave(
                            studyValue,
                            shortValue,
                            longValue,
                            cycleValue
                        )
                    }
                }
            ) {
                Text(
                    text = "Guardar",
                    color = LumiColors.YellowStrong,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = LumiColors.Dark
                )
            }
        }
    )
}

@Composable
private fun SettingsNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}
