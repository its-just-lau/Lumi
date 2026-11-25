package com.lumi.app.features.tasks.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.data.models.tasks.TaskResponse
import com.lumi.app.features.tasks.dueDateOrNull
import com.lumi.app.features.tasks.dueTimeOrNull
import com.lumi.app.features.tasks.isCompleted
import com.lumi.app.features.tasks.isOverdue
import com.lumi.app.ui.theme.LumiColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

private fun dayFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern(AppText.get(R.string.pattern_short_day), Locale.getDefault())

fun priorityLabel(priority: String): String = when (priority) {
    "high" -> AppText.get(R.string.alta)
    "low" -> AppText.get(R.string.baja)
    else -> AppText.get(R.string.media)
}

fun priorityColor(priority: String): Color = when (priority) {
    "high" -> Color(0xFFFFB8A2)
    "low" -> Color(0xFFBCE6C5)
    else -> LumiColors.Yellow
}

/** Texto de vencimiento: "Hoy 14:30", "Mañana", "25 nov"... */
private fun dueText(task: TaskResponse, today: LocalDate): String? {
    val date = task.dueDateOrNull() ?: return null
    val time = task.dueTimeOrNull()

    val day = when (date) {
        today -> AppText.get(R.string.hoy)
        today.plusDays(1) -> AppText.get(R.string.manana)
        else -> date.format(dayFormatter())
    }

    return if (time != null) AppText.get(R.string.text_5, day, time.toString().take(5)) else day
}

@Composable
fun TaskCard(
    task: TaskResponse,
    subjectName: String?,
    now: LocalDateTime,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit
) {
    val completed = task.isCompleted()
    val overdue = task.isOverdue(now)
    val subtasks = task.subtasks.orEmpty().filter { it.deletedAt == null }
    val dueLabel = dueText(task, now.toLocalDate())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo para completar / reabrir
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .then(
                        if (completed) {
                            Modifier.background(LumiColors.Yellow)
                        } else {
                            Modifier.border(BorderStroke(2.dp, LumiColors.Dark), CircleShape)
                        }
                    )
                    .clickable(onClick = onToggleComplete),
                contentAlignment = Alignment.Center
            ) {
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.tarea_completada_toca_para_reabrir),
                        tint = LumiColors.Dark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark.copy(alpha = if (completed) 0.5f else 1f),
                    textDecoration = if (completed) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val details = listOfNotNull(
                    subjectName,
                    dueLabel,
                    if (subtasks.isNotEmpty()) {
                        AppText.get(R.string.subtareas_2, subtasks.count { it.completed }, subtasks.size)
                    } else null
                )

                if (details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = details.joinToString(" · "),
                        fontSize = 13.sp,
                        color = if (overdue) Color.Red else LumiColors.Dark.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(priorityColor(task.priority))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = priorityLabel(task.priority),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            }
        }
    }
}
