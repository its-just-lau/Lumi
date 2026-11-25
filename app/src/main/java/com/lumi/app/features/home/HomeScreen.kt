
package com.lumi.app.features.home

import com.lumi.app.core.components.LumiIcon
import com.lumi.app.core.components.LumiIconKind
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrackChanges
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.lumi.app.data.models.tasks.TaskResponse
import com.lumi.app.features.tasks.TaskViewModel
import com.lumi.app.features.tasks.TaskViewModelFactory
import com.lumi.app.features.tasks.TasksUiState
import com.lumi.app.features.tasks.dueDateOrNull
import com.lumi.app.features.tasks.isCompleted
import com.lumi.app.features.calendar.CalendarViewModel
import com.lumi.app.features.calendar.CalendarViewModelFactory
import com.lumi.app.features.calendar.EventDialog
import com.lumi.app.features.calendar.GoogleCalendarPrefs
import com.lumi.app.features.calendar.rememberGoogleCalendarAccess
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.core.components.SkeletonBox
import com.lumi.app.features.stats.StatsViewModel
import com.lumi.app.features.stats.StatsViewModelFactory
import com.lumi.app.ui.theme.LumiColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

private data class DashboardTask(
    val id: String,
    val title: String,
    val date: LocalDate,
    val isDone: Boolean,
    val source: TaskResponse
)

// Meta diaria usada si el usuario aún no ha configurado la suya (en minutos)
private const val DEFAULT_DAILY_GOAL = 60

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    val container = LumiApp.container
    val context = LocalContext.current

    // Nombre real del usuario
    val userName by produceState("") {
        value = container.userRepository.getProfile().getOrNull()?.name.orEmpty()
    }

    val statsViewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(LumiApp.container.progressRepository)
    )
    val userStats by statsViewModel.stats.collectAsState()
    val gems = userStats.gems
    val streakDays = userStats.streakDays

    LaunchedEffect(Unit) {
        statsViewModel.load()
    }

    // Meta semanal: minutos estudiados esta semana vs. meta diaria x 7 de las preferencias
    val weeklyStudiedMinutes by produceState(0) {
        value = container.progressRepository.getWeekly().getOrNull()
            ?.dailyMinutes?.sum() ?: 0
    }
    val weeklyGoalMinutes by produceState(DEFAULT_DAILY_GOAL * 7) {
        val daily = container.userRepository.getPreferences().getOrNull()?.dailyGoalMinutes
        value = (daily ?: DEFAULT_DAILY_GOAL) * 7
    }

    val weeklyProgress =
        weeklyStudiedMinutes.toFloat() / weeklyGoalMinutes.coerceAtLeast(1)

    val today = remember { LocalDate.now() }

    // FECHA SELECCIONADA EN EL CALENDARIO
    var selectedDate by remember { mutableStateOf(today) }

    var displayedMonth by remember {
        mutableStateOf(YearMonth.from(today))
    }

    // TAREAS REALES (las mismas de la pantalla Tareas)
    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            container.taskRepository,
            container.subjectRepository
        )
    )
    val tasksState by taskViewModel.uiState.collectAsState()
    val taskMessage by taskViewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        taskViewModel.loadTasks()
    }

    LaunchedEffect(taskMessage) {
        taskMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            taskViewModel.consumeMessage()
            statsViewModel.load()
        }
    }

    val tasks = (tasksState as? TasksUiState.Success)?.tasks.orEmpty()
        .filter { it.deletedAt == null }
        .mapNotNull { task ->
            task.dueDateOrNull()?.let { date ->
                DashboardTask(task.id, task.title, date, task.isCompleted(), task)
            }
        }

    // GOOGLE CALENDAR
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(container.googleCalendarRepository)
    )
    val googleEvents by calendarViewModel.events.collectAsState()
    val calendarSaving by calendarViewModel.saving.collectAsState()
    val calendarMessage by calendarViewModel.message.collectAsState()

    val calendarPrefs = remember { GoogleCalendarPrefs(context) }
    var calendarConnected by remember { mutableStateOf(calendarPrefs.connected) }
    var showEventDialog by remember { mutableStateOf(false) }

    // El token de Google dura 1 hora; se reutiliza mientras siga vigente
    var googleToken by remember { mutableStateOf<String?>(null) }
    var googleTokenTime by remember { mutableLongStateOf(0L) }

    val calendarAccess = rememberGoogleCalendarAccess(
        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    )

    fun withGoogleToken(interactive: Boolean, block: (String) -> Unit) {
        val cached = googleToken
        val fresh = System.currentTimeMillis() - googleTokenTime < 50 * 60 * 1000L

        if (cached != null && fresh) {
            block(cached)
        } else {
            calendarAccess.request(interactive) { token ->
                googleToken = token
                googleTokenTime = System.currentTimeMillis()
                block(token)
            }
        }
    }

    // Carga los eventos del mes mostrado (sin mostrar pantallas de Google si ya hay permiso)
    LaunchedEffect(calendarConnected, displayedMonth) {
        if (calendarConnected) {
            withGoogleToken(interactive = false) { token ->
                calendarViewModel.loadMonth(token, displayedMonth)
            }
        }
    }

    LaunchedEffect(calendarMessage) {
        calendarMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            calendarViewModel.consumeMessage()
        }
    }

    // Días del calendario con tareas pendientes o eventos de Google
    val eventDates = tasks.filter { !it.isDone }.map { it.date }.toSet() +
            googleEvents.map { it.date }.toSet()

    // TAREAS DEL DÍA SELECCIONADO Y LOS DOS SIGUIENTES
    val upcomingTasks = tasks.filter {
        !it.date.isBefore(selectedDate) &&
                !it.date.isAfter(selectedDate.plusDays(2))
    }

    Scaffold(
        containerColor = LumiColors.Cream,
        bottomBar = {
            LumiBottomBar(
                selectedRoute = "home",
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. PERFIL Y GEMAS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onNavigate("profile")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.ir_al_perfil),
                        tint = LumiColors.YellowStrong,
                        modifier = Modifier.size(52.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (userName.isBlank()) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(130.dp)
                                .height(20.dp)
                        )
                    } else {
                        Text(
                            text = userName,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LumiColors.Dark
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(LumiColors.White)
                        .clickable {
                            onNavigate("store")
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LumiIcon(LumiIconKind.Gem, size = 28.dp)

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = gems.toString(),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.YellowStrong
                    )
                }
            }

            // 2. PANEL DE RACHA CON LUMI
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LumiColors.Yellow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LumiIcon(
                            if (streakDays > 0) LumiIconKind.Flame else LumiIconKind.FlameOff,
                            size = 60.dp
                        )

                        Text(
                            text = streakDays.toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )

                        Text(
                            text = stringResource(R.string.dias_de_racha),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        com.lumi.app.mascot.MascotAnimation(
                            resId = if (streakDays > 0) {
                                com.lumi.app.mascot.LumiAnimations.STREAK_ACTIVE
                            } else {
                                com.lumi.app.mascot.LumiAnimations.STREAK_NONE
                            },
                            modifier = Modifier
                                .height(150.dp)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(18.dp))
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.lo_estas_haciendo_genial),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. LECCIÓN RECOMENDADA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigate("study")
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF665000)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LumiIcon(LumiIconKind.Book, size = 26.dp)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.continua_aprendiendo),
                            color = LumiColors.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = LumiColors.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.recursividad_en_kotlin),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.programacion_movil_2_de_5),
                        fontSize = 13.sp,
                        color = LumiColors.Yellow
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { 0.4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(CircleShape),
                        color = LumiColors.Yellow,
                        trackColor = Color(0xFF8D793A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onNavigate("study")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LumiColors.Cream,
                            contentColor = LumiColors.Dark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.continuar_leccion),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. META SEMANAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LumiColors.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = LumiColors.YellowStrong,
                            modifier = Modifier.size(38.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.meta_semanal),
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark
                            )

                            Text(
                                text = "${weeklyStudiedMinutes / 60} h ${
                                    weeklyStudiedMinutes % 60
                                } min de ${
                                    weeklyGoalMinutes / 60
                                } h",
                                color = LumiColors.Dark,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = stringResource(R.string.text, (weeklyProgress * 100).toInt()),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.YellowStrong
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = {
                            weeklyProgress.coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(11.dp)
                            .clip(CircleShape),
                        color = LumiColors.YellowStrong,
                        trackColor = LumiColors.WarmGray
                    )
                }
            }

            // 5. CALENDARIO Y CHECKLIST
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {

                // COLUMNA IZQUIERDA: CALENDARIO
                Card(
                    modifier = Modifier.weight(1.05f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LumiColors.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = LumiColors.YellowStrong,
                                modifier = Modifier.size(19.dp)
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            Text(
                                text = stringResource(R.string.calendario),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark,
                                modifier = Modifier.weight(1f)
                            )

                            // Agregar evento a Google Calendar
                            IconButton(
                                onClick = {
                                    withGoogleToken(interactive = true) {
                                        calendarPrefs.connected = true
                                        calendarConnected = true
                                        showEventDialog = true
                                    }
                                },
                                modifier = Modifier.size(25.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.agregar_evento_a_google_calendar),
                                    tint = LumiColors.Dark,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = {
                                    displayedMonth =
                                        displayedMonth.minusMonths(1)
                                },
                                modifier = Modifier.size(25.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = stringResource(R.string.mes_anterior),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = displayedMonth.month
                                    .getDisplayName(
                                        java.time.format.TextStyle.FULL,
                                        androidx.compose.ui.text.intl.Locale.current.platformLocale
                                    )
                                    .replaceFirstChar {
                                        it.uppercase()
                                    } + " ${displayedMonth.year}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    displayedMonth =
                                        displayedMonth.plusMonths(1)
                                },
                                modifier = Modifier.size(25.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = stringResource(R.string.mes_siguiente),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        CalendarPreview(
                            month = displayedMonth,
                            selectedDate = selectedDate,
                            eventDates = eventDates,
                            onDateSelected = {
                                selectedDate = it
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        LumiColors.YellowStrong,
                                        CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = stringResource(R.string.eventos),
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // COLUMNA DERECHA: CHECKLIST
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LumiColors.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.mis_tareas),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (tasksState is TasksUiState.Idle || tasksState is TasksUiState.Loading) {
                            repeat(3) {
                                SkeletonBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(14.dp)
                                )
                            }
                        } else if (upcomingTasks.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_tienes_tareas_pendientes_para),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        } else {

                            upcomingTasks
                                .groupBy { it.date }
                                .toSortedMap()
                                .forEach { (date, dayTasks) ->

                                    val dayLabel = when (date) {
                                        selectedDate -> stringResource(R.string.dia_seleccionado)
                                        selectedDate.plusDays(1) -> stringResource(R.string.siguiente_dia)
                                        else -> stringResource(R.string.dos_dias_despues)
                                    }

                                    Text(
                                        text = dayLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LumiColors.YellowStrong
                                    )

                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    dayTasks.forEach { task ->

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = task.isDone,
                                                onCheckedChange = {
                                                    taskViewModel.toggleComplete(task.source)
                                                },
                                                modifier = Modifier.size(25.dp),
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor =
                                                        LumiColors.YellowStrong,
                                                    checkmarkColor =
                                                        LumiColors.Dark
                                                )
                                            )

                                            Spacer(
                                                modifier = Modifier.width(3.dp)
                                            )

                                            Text(
                                                text = task.title,
                                                fontSize = 10.sp,
                                                color = LumiColors.Dark,
                                                lineHeight = 13.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(7.dp)
                                    )
                                }
                        }

                        // EVENTOS DE GOOGLE CALENDAR
                        val windowEvents = googleEvents
                            .filter {
                                !it.date.isBefore(selectedDate) &&
                                        !it.date.isAfter(selectedDate.plusDays(2))
                            }
                            .sortedWith(compareBy({ it.date }, { it.time ?: "" }))

                        if (windowEvents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(R.string.eventos_de_google_calendar),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.YellowStrong
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            windowEvents.forEach { event ->
                                Text(
                                    text = stringResource(
                                        R.string.event_line,
                                        event.date.dayOfMonth,
                                        event.date.monthValue,
                                        event.time ?: stringResource(R.string.todo_el_dia),
                                        event.title
                                    ),
                                    fontSize = 10.sp,
                                    color = LumiColors.Dark,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showEventDialog) {
        EventDialog(
            date = selectedDate,
            saving = calendarSaving,
            onDismiss = { showEventDialog = false },
            onSave = { title, description, allDay, start, end ->
                withGoogleToken(interactive = true) { token ->
                    calendarViewModel.createEvent(
                        accessToken = token,
                        title = title,
                        description = description,
                        date = selectedDate,
                        allDay = allDay,
                        start = start,
                        end = end,
                        onCreated = { showEventDialog = false }
                    )
                }
            }
        )
    }
}

// CALENDARIO MENSUAL INTERACTIVO
@Composable
private fun CalendarPreview(
    month: YearMonth,
    selectedDate: LocalDate,
    eventDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val weekdays = listOf("D", "L", "M", "M", "J", "V", "S")

    val firstDayOffset = month.atDay(1).dayOfWeek.value % 7

    val daysInMonth = month.lengthOfMonth()

    val calendarCells = List(firstDayOffset) {
        null
    } + (1..daysInMonth).map { it }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        calendarCells.chunked(7).forEach { week ->

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                week.forEach { day ->

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(37.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        if (day != null) {

                            val date = month.atDay(day)
                            val isSelected = date == selectedDate
                            val hasEvent = date in eventDates

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .clickable {
                                        onDateSelected(date)
                                    },
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.Center
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(23.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                LumiColors.Dark
                                            } else {
                                                Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                        color = if (isSelected) {
                                            LumiColors.White
                                        } else if (
                                            date.dayOfWeek.value == 7
                                        ) {
                                            Color(0xFFCC6666)
                                        } else {
                                            LumiColors.Dark
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (hasEvent) {
                                                LumiColors.YellowStrong
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                // Completa la última semana con espacios vacíos
                repeat(7 - week.size) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(37.dp)
                    )
                }
            }
        }
    }
}
