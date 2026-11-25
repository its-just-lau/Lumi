package com.lumi.app.features.study

import com.lumi.app.core.components.LumiEmoji
import com.lumi.app.mascot.MascotCelebration
import com.lumi.app.mascot.MascotAnimation
import com.lumi.app.mascot.LumiAnimations
import com.lumi.app.core.components.LumiIcon
import com.lumi.app.core.components.LumiIconKind
import androidx.compose.foundation.BorderStroke
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
import com.lumi.app.core.components.SkeletonList
import com.lumi.app.ui.theme.LumiColors
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.features.stats.StatsViewModel
import com.lumi.app.features.stats.StatsViewModelFactory
import com.lumi.app.features.study.learning.FlashcardsScreen
import com.lumi.app.features.study.learning.LearningContent
import com.lumi.app.features.study.learning.LearningUiState
import com.lumi.app.features.study.learning.LearningViewModel
import com.lumi.app.features.study.learning.LearningViewModelFactory
import com.lumi.app.features.study.learning.LessonItem
import com.lumi.app.features.study.learning.LessonProgressStore
import com.lumi.app.features.study.learning.LessonScreen
import com.lumi.app.features.study.learning.QuizScreen
import com.lumi.app.features.study.materials.MaterialsScreen
import java.time.Instant
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

@Composable
fun StudyScreen(
    onNavigate: (String) -> Unit
) {
    // DATOS PROVISIONALES
    val container = LumiApp.container
    val context = LocalContext.current

    // Encabezado: gemas, racha y XP reales
    val statsViewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(container.progressRepository)
    )
    val userStats by statsViewModel.stats.collectAsState()
    val gems = userStats.gems
    val streakDays = userStats.streakDays
    val weeklyXp = userStats.weeklyXp

    LaunchedEffect(Unit) {
        statsViewModel.load()
    }

    val studyViewModel: StudyViewModel = viewModel(
        factory = StudyViewModelFactory(
            container.studyRepository,
            container.subjectRepository
        )
    )

    val userSubjects by studyViewModel.subjects.collectAsState()
    val studyMessage by studyViewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        studyViewModel.loadSubjects()
    }

    LaunchedEffect(studyMessage) {
        studyMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            studyViewModel.consumeMessage()
            statsViewModel.load()
        }
    }

    // Opciones del selector: (id de la materia, nombre)
    val subjects: List<Pair<String?, String>> =
        listOf<Pair<String?, String>>(null to AppText.get(R.string.sin_materia)) +
                userSubjects.map { it.id to it.name }

    var selectedSubjectId by remember {
        mutableStateOf<String?>(null)
    }

    val selectedSubject = subjects
        .firstOrNull { it.first == selectedSubjectId }
        ?.second ?: stringResource(R.string.sin_materia)

    var showMaterials by remember {
        mutableStateOf(false)
    }

    // CONTENIDO GENERADO (lecciones, tarjetas y cuestionarios)
    val learningViewModel: LearningViewModel = viewModel(
        factory = LearningViewModelFactory(container.materialRepository)
    )
    val learningState by learningViewModel.uiState.collectAsState()
    val quizState by learningViewModel.quizState.collectAsState()
    val learningMessage by learningViewModel.message.collectAsState()
    val progressStore = remember { LessonProgressStore(context) }

    var learningOverlay by remember { mutableStateOf<String?>(null) }
    var openLesson by remember { mutableStateOf<LessonItem?>(null) }
    var progressVersion by remember { mutableIntStateOf(0) }

    // Recarga al abrir y cada vez que se vuelve de Materiales / tarjetas / cuestionario / lección
    LaunchedEffect(showMaterials, learningOverlay, openLesson) {
        if (!showMaterials && learningOverlay == null && openLesson == null) {
            learningViewModel.load()
        }
    }

    LaunchedEffect(learningMessage) {
        learningMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            learningViewModel.consumeMessage()
            statsViewModel.load()
        }
    }

    val content = (learningState as? LearningUiState.Success)?.content ?: LearningContent()

    // Se lee progressVersion para refrescar los porcentajes al volver de una lección
    val lessonProgress: (LessonItem) -> Int = { item ->
        progressVersion
        progressStore.get(item.lesson.id)
    }

    val recommendedLesson = content.lessons
        .filter { lessonProgress(it) < 100 }
        .maxByOrNull { lessonProgress(it) }

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
    var showSessionDone by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // REGISTRO DE LA SESIÓN (se guarda en la API al terminar)
    var sessionStartedAt by remember { mutableStateOf<Instant?>(null) }
    var pausedSeconds by remember { mutableIntStateOf(0) }

    fun endSession(save: Boolean) {
        val started = sessionStartedAt

        if (save && started != null) {
            studyViewModel.saveSession(
                subjectId = selectedSubjectId,
                isPomodoro = isPomodoro,
                plannedMinutes = studyMinutes,
                pausedSeconds = pausedSeconds,
                startedAt = started,
                endedAt = Instant.now()
            )
        }

        sessionStartedAt = null
        pausedSeconds = 0
    }

    // Cuenta el tiempo en pausa mientras hay una sesión iniciada
    LaunchedEffect(isRunning, sessionStartedAt) {
        while (!isRunning && sessionStartedAt != null) {
            delay(1000)
            pausedSeconds++
        }
    }

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

                    // Pomodoro completado: se registra la sesión
                    endSession(save = true)
                    showSessionDone = true
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

    if (showSessionDone) {
        MascotCelebration(
            animationRes = LumiAnimations.SESSION_DONE,
            title = stringResource(R.string.sesion_terminada),
            onDismiss = { showSessionDone = false }
        )
    }

    // Pantalla de materiales a pantalla completa (el temporizador sigue en memoria)
    if (showMaterials) {
        MaterialsScreen(onBack = { showMaterials = false })
        return
    }

    if (learningOverlay == "flashcards") {
        FlashcardsScreen(
            cards = content.flashcards,
            onReview = { id, result -> learningViewModel.reviewFlashcard(id, result) },
            onBack = { learningOverlay = null }
        )
        return
    }

    if (learningOverlay == "quiz") {
        QuizScreen(
            quizzes = content.quizzes,
            quizState = quizState,
            onSubmit = { quizItem, selected -> learningViewModel.submitQuiz(quizItem.quiz, selected) },
            onReset = { learningViewModel.resetQuiz() },
            onBack = { learningOverlay = null }
        )
        return
    }

    openLesson?.let { lessonItem ->
        LessonScreen(
            item = lessonItem,
            onProgress = { progressStore.save(lessonItem.lesson.id, it) },
            onComplete = {
                progressStore.save(lessonItem.lesson.id, 100)
                learningViewModel.completeLesson(lessonItem.lesson.id)
                progressVersion++
                openLesson = null
            },
            onBack = {
                progressVersion++
                openLesson = null
            }
        )
        return
    }

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
                    text = stringResource(R.string.estudiar),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LumiIcon(LumiIconKind.Gem, size = 20.dp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$gems",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LumiIcon(
                            if (streakDays > 0) LumiIconKind.Flame else LumiIconKind.FlameOff,
                            size = 20.dp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$streakDays",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LumiIcon(LumiIconKind.Star, size = 20.dp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.xp, weeklyXp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )
                    }
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
                        MascotAnimation(
                            resId = when {
                                isRunning -> LumiAnimations.STUDYING
                                sessionStartedAt != null -> LumiAnimations.BREAK
                                else -> LumiAnimations.REST
                            },
                            modifier = Modifier
                                .height(96.dp)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(14.dp))
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
                                    AppText.get(R.string.muy_bien_sigue_concentrandote)
                                } else {
                                    AppText.get(R.string.vamos_a_aprovechar_el_tiempo)
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
                            subjects.forEach { (subjectId, subjectName) ->
                                DropdownMenuItem(
                                    text = { Text(subjectName) },
                                    onClick = {
                                        selectedSubjectId = subjectId
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
                                    AppText.get(R.string.enfoque_sesion)
                                } else {
                                    AppText.get(R.string.tiempo_libre)
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
                                    endSession(save = false)
                                    isPomodoro = true
                                    isRunning = false
                                    remainingSeconds = studyMinutes * 60
                                    elapsedSeconds = 0
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.pomodoro, studyMinutes, shortBreakMinutes),
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
                                    endSession(save = false)
                                    isPomodoro = false
                                    isRunning = false
                                    elapsedSeconds = 0
                                    remainingSeconds = studyMinutes * 60
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.tiempo_libre),
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

                                if (!isRunning && sessionStartedAt == null) {
                                    sessionStartedAt = Instant.now()
                                    pausedSeconds = 0
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
                                text = if (isRunning) stringResource(R.string.pausar_sesion)
                                else stringResource(R.string.iniciar_sesion),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                // En tiempo libre, reiniciar guarda la sesión si duró al menos 1 minuto
                                endSession(save = !isPomodoro && elapsedSeconds >= 60)

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
                                contentDescription = stringResource(R.string.reiniciar),
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
                                contentDescription = stringResource(R.string.configurar_pomodoro),
                                tint = LumiColors.Dark
                            )
                        }
                    }

                    if (sessionStartedAt != null) {
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val studiedSeconds = if (isPomodoro) {
                                    studyMinutes * 60 - remainingSeconds
                                } else {
                                    elapsedSeconds
                                }

                                if (studiedSeconds >= 60) {
                                    endSession(save = true)
                                } else {
                                    endSession(save = false)
                                    Toast.makeText(
                                        context,
                                        AppText.get(R.string.la_sesion_duro_menos_de),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                isRunning = false
                                elapsedSeconds = 0
                                remainingSeconds = studyMinutes * 60
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, LumiColors.Dark),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = LumiColors.Dark
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.finalizar_sesion),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (recommendedLesson != null) {
                // LECCIÓN RECOMENDADA
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openLesson = recommendedLesson },
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
                                text = stringResource(R.string.leccion_recomendada),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Yellow
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = recommendedLesson.lesson.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.White
                            )

                            Text(
                                text = progressLabel(lessonProgress(recommendedLesson)),
                                fontSize = 13.sp,
                                color = LumiColors.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LumiColors.Yellow)
                                .clickable { openLesson = recommendedLesson },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.abrir_leccion),
                                tint = LumiColors.Dark
                            )
                        }
                    }
                }

            }

            // LECCIONES GUIADAS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.lecciones_guiadas),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                if (learningState is LearningUiState.Loading) {
                    SkeletonList(count = 3)
                } else if (content.lessons.isEmpty()) {
                    Text(
                        text = stringResource(R.string.aun_no_tienes_lecciones_sube),
                        fontSize = 13.sp,
                        color = LumiColors.Dark.copy(alpha = 0.65f)
                    )
                }

                content.lessons.forEach { item ->
                    GuidedLessonCard(
                        title = item.lesson.title,
                        subject = item.materialTitle,
                        detail = progressLabel(lessonProgress(item)),
                        emoji = "📘",
                        onClick = { openLesson = item }
                    )
                }
            }

            // ACCESOS RÁPIDOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStudyCard(
                    emoji = "▤",
                    title = stringResource(R.string.tarjetas),
                    modifier = Modifier.weight(1f),
                    onClick = { learningOverlay = "flashcards" }
                )

                QuickStudyCard(
                    emoji = "❔",
                    title = stringResource(R.string.cuestionario),
                    modifier = Modifier.weight(1f),
                    onClick = { learningOverlay = "quiz" }
                )

                QuickStudyCard(
                    emoji = "📄",
                    title = stringResource(R.string.materiales),
                    modifier = Modifier.weight(1f),
                    onClick = { showMaterials = true }
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

                endSession(save = false)

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
    detail: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                LumiEmoji(emoji, size = 26.dp)
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
                    text = detail,
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(105.dp)
            .clickable(onClick = onClick),
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
            LumiEmoji(emoji, size = 29.dp)

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
                text = stringResource(R.string.configurar_pomodoro),
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.personaliza_tus_tiempos_en_minutos),
                    color = LumiColors.Dark,
                    fontSize = 13.sp
                )

                SettingsNumberField(
                    label = stringResource(R.string.tiempo_de_estudio),
                    value = study,
                    onValueChange = { study = it }
                )

                SettingsNumberField(
                    label = stringResource(R.string.descanso_corto),
                    value = shortBreak,
                    onValueChange = { shortBreak = it }
                )

                SettingsNumberField(
                    label = stringResource(R.string.descanso_largo),
                    value = longBreak,
                    onValueChange = { longBreak = it }
                )

                SettingsNumberField(
                    label = stringResource(R.string.ciclos_antes_del_descanso_largo),
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
                    text = stringResource(R.string.guardar),
                    color = LumiColors.YellowStrong,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancelar),
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

private fun progressLabel(percent: Int): String = when {
    percent >= 100 -> AppText.get(R.string.completada)
    percent > 0 -> AppText.get(R.string.avance, percent)
    else -> AppText.get(R.string.nueva)
}
