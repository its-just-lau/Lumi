package com.lumi.app.features.calendar

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiTextField
import com.lumi.app.ui.theme.LumiColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

/** Formulario para crear un evento en Google Calendar en la fecha seleccionada. */
@Composable
fun EventDialog(
    date: LocalDate,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, allDay: Boolean, start: LocalTime, end: LocalTime) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(false) }
    var start by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var end by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var error by remember { mutableStateOf<String?>(null) }

    val dateText = date.format(
        DateTimeFormatter.ofPattern(AppText.get(R.string.pattern_event_date), Locale.getDefault())
    ).replaceFirstChar { it.uppercase() }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = LumiColors.White,
        title = {
            Text(
                text = stringResource(R.string.nuevo_evento),
                color = LumiColors.Dark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.google_calendar, dateText),
                    fontSize = 13.sp,
                    color = LumiColors.Dark.copy(alpha = 0.7f)
                )

                LumiTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        error = null
                    },
                    label = stringResource(R.string.titulo)
                )

                LumiTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.descripcion_opcional)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.todo_el_dia),
                        modifier = Modifier.weight(1f),
                        color = LumiColors.Dark
                    )
                    Switch(
                        checked = allDay,
                        onCheckedChange = { allDay = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = LumiColors.YellowStrong,
                            checkedThumbColor = LumiColors.Dark
                        )
                    )
                }

                if (!allDay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        start = LocalTime.of(h, m)
                                        error = null
                                    },
                                    start.hour, start.minute, true
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.inicio_2, start.toString().take(5)), color = LumiColors.Dark)
                        }

                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        end = LocalTime.of(h, m)
                                        error = null
                                    },
                                    end.hour, end.minute, true
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.fin, end.toString().take(5)), color = LumiColors.Dark)
                        }
                    }
                }

                error?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = androidx.compose.ui.graphics.Color(0xFFD32F2F)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    when {
                        title.isBlank() -> error = AppText.get(R.string.escribe_un_titulo_para_el)
                        !allDay && !end.isAfter(start) ->
                            error = AppText.get(R.string.la_hora_de_fin_debe)
                        else -> onSave(title, description, allDay, start, end)
                    }
                }
            ) {
                Text(
                    text = if (saving) "Guardando..." else stringResource(R.string.guardar),
                    color = LumiColors.YellowStrong,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text(stringResource(R.string.cancelar), color = LumiColors.Dark)
            }
        }
    )
}
