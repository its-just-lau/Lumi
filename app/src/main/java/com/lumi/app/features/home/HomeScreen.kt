
package com.lumi.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.ui.theme.LumiColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class DashboardTask(
    val id: Int,
    val title: String,
    val date: LocalDate,
    val isDone: Boolean = false
)

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    // DATOS PROVISIONALES
    val userName = "Laura Chávez"
    val gems = 1439
    val streakDays = 20

    val weeklyStudiedMinutes = 275
    val weeklyGoalMinutes = 360

    val weeklyProgress =
        weeklyStudiedMinutes.toFloat() / weeklyGoalMinutes

    val today = remember { LocalDate.now() }

    // FECHA SELECCIONADA EN EL CALENDARIO
    var selectedDate by remember { mutableStateOf(today) }

    var displayedMonth by remember {
        mutableStateOf(YearMonth.from(today))
    }

    // TAREAS PROVISIONALES
    val tasks = remember {
        mutableStateListOf(
            DashboardTask(1, "Terminar ejercicios de Kotlin", today),
            DashboardTask(2, "Repasar apuntes de Bases de Datos", today),
            DashboardTask(3, "Preparar exposición", today.plusDays(1)),
            DashboardTask(4, "Realizar práctica de programación", today.plusDays(2)),
            DashboardTask(5, "Estudiar para el examen", today.plusDays(2))
        )
    }

    // EVENTOS PROVISIONALES DEL CALENDARIO
    val eventDates = remember(today) {
        setOf(
            today,
            today.plusDays(2),
            today.plusDays(5),
            today.plusDays(8),
            today.plusDays(12)
        )
    }

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
                        contentDescription = "Ir al perfil",
                        tint = LumiColors.YellowStrong,
                        modifier = Modifier.size(52.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = userName,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LumiColors.Dark
                    )
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
                    Text(
                        text = "💎",
                        fontSize = 23.sp
                    )

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
                        Text(
                            text = "🔥",
                            fontSize = 55.sp
                        )

                        Text(
                            text = streakDays.toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )

                        Text(
                            text = "días de racha",
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
                        Text(
                            text = "🦎",
                            fontSize = 78.sp
                        )

                        Text(
                            text = "¡Lo estás haciendo genial!",
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
                        Text(
                            text = "📚",
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Continúa aprendiendo",
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
                        text = "Recursividad en Kotlin",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Programación móvil · 2 de 5 actividades",
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
                            text = "Continuar lección",
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
                                text = "Meta semanal",
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
                            text = "${(weeklyProgress * 100).toInt()}%",
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
                                text = "Calendario",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark
                            )
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
                                    contentDescription = "Mes anterior",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = displayedMonth.month
                                    .getDisplayName(
                                        java.time.format.TextStyle.FULL,
                                        Locale("es", "MX")
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
                                    contentDescription = "Mes siguiente",
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
                                text = "Eventos",
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
                            text = "Mis tareas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (upcomingTasks.isEmpty()) {
                            Text(
                                text = "No tienes tareas pendientes para estos días.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        } else {

                            upcomingTasks
                                .groupBy { it.date }
                                .toSortedMap()
                                .forEach { (date, dayTasks) ->

                                    val dayLabel = when (date) {
                                        selectedDate -> "Día seleccionado"
                                        selectedDate.plusDays(1) -> "Siguiente día"
                                        else -> "Dos días después"
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
                                                onCheckedChange = { checked ->
                                                    val index = tasks.indexOfFirst {
                                                        it.id == task.id
                                                    }

                                                    if (index != -1) {
                                                        tasks[index] =
                                                            tasks[index].copy(
                                                                isDone = checked
                                                            )
                                                    }
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
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
