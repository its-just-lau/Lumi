package com.lumi.app.features.tasks.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.core.components.LumiTextField
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.data.models.tasks.TaskResponse
import com.lumi.app.features.subjects.components.ConfirmDeleteDialog
import com.lumi.app.features.tasks.TaskOperationState
import com.lumi.app.features.tasks.dueDateOrNull
import com.lumi.app.features.tasks.dueTimeOrNull
import com.lumi.app.features.tasks.model.SubtaskDraft
import com.lumi.app.features.tasks.model.TaskForm
import com.lumi.app.ui.theme.LumiColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

private fun dateFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern(AppText.get(R.string.pattern_task_date), Locale.getDefault())

/**
 * Formulario a pantalla completa para crear o editar una tarea (con sus subtareas).
 * Si [initial] es null se crea una tarea nueva.
 */
@Composable
fun TaskFormScreen(
    initial: TaskResponse?,
    subjects: List<SubjectResponse>,
    operationState: TaskOperationState,
    onSave: (TaskForm) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = initial != null
    val isLoading = operationState is TaskOperationState.Loading
    val errorMessage = (operationState as? TaskOperationState.Error)?.message

    var title by remember(initial?.id, initial?.version) { mutableStateOf(initial?.title ?: "") }
    var description by remember(initial?.id, initial?.version) { mutableStateOf(initial?.description ?: "") }
    var subjectId by remember(initial?.id, initial?.version) { mutableStateOf(initial?.subjectId) }
    var dueDate by remember(initial?.id, initial?.version) { mutableStateOf(initial?.dueDateOrNull()) }
    var dueTime by remember(initial?.id, initial?.version) { mutableStateOf(initial?.dueTimeOrNull()) }
    var allDay by remember(initial?.id, initial?.version) {
        mutableStateOf(initial?.let { it.allDay || it.dueTimeOrNull() == null } ?: true)
    }
    var priority by remember(initial?.id, initial?.version) { mutableStateOf(initial?.priority ?: "medium") }

    val subtasks = remember(initial?.id, initial?.version) {
        mutableStateListOf<SubtaskDraft>().apply {
            initial?.subtasks.orEmpty()
                .filter { it.deletedAt == null }
                .forEach { add(SubtaskDraft(it.id, it.title, it.completed)) }
        }
    }

    var newSubtask by remember { mutableStateOf("") }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !isLoading) { onBack() }

    fun addSubtask() {
        val text = newSubtask.trim()

        if (text.isNotEmpty()) {
            subtasks.add(SubtaskDraft(id = UUID.randomUUID().toString(), title = text))
            newSubtask = ""
        }
    }

    Scaffold(containerColor = LumiColors.Cream) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ENCABEZADO
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, enabled = !isLoading) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.volver),
                        tint = LumiColors.Dark
                    )
                }

                Text(
                    text = if (isEditing) stringResource(R.string.editar_tarea) else stringResource(R.string.nueva_tarea),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LumiTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(R.string.titulo)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.descripcion_opcional)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = taskFieldColors()
                )

                // MATERIA
                SectionTitle(stringResource(R.string.materia))

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LumiColors.White)
                            .border(1.dp, LumiColors.WarmGray, RoundedCornerShape(14.dp))
                            .clickable { subjectMenuExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subjects.firstOrNull { it.id == subjectId }?.name
                                ?: stringResource(R.string.sin_materia),
                            modifier = Modifier.weight(1f),
                            color = LumiColors.Dark
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = LumiColors.Dark
                        )
                    }

                    DropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sin_materia)) },
                            onClick = {
                                subjectId = null
                                subjectMenuExpanded = false
                            }
                        )

                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    subjectId = subject.id
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // FECHA Y HORA
                SectionTitle(stringResource(R.string.fecha_de_entrega))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PickerButton(
                        text = dueDate?.format(dateFormatter()) ?: stringResource(R.string.elegir_fecha),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val base = dueDate ?: LocalDate.now()

                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    dueDate = LocalDate.of(year, month + 1, day)
                                },
                                base.year,
                                base.monthValue - 1,
                                base.dayOfMonth
                            ).show()
                        }
                    )

                    if (dueDate != null) {
                        IconButton(onClick = { dueDate = null; dueTime = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.quitar_fecha),
                                tint = LumiColors.Dark
                            )
                        }
                    }
                }

                if (dueDate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.todo_el_dia),
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium,
                            color = LumiColors.Dark
                        )

                        Switch(
                            checked = allDay,
                            onCheckedChange = { allDay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LumiColors.Dark,
                                checkedTrackColor = LumiColors.Yellow,
                                uncheckedThumbColor = LumiColors.Dark,
                                uncheckedTrackColor = LumiColors.WarmGray
                            )
                        )
                    }

                    if (!allDay) {
                        PickerButton(
                            text = dueTime?.toString()?.take(5) ?: stringResource(R.string.elegir_hora),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val base = dueTime ?: LocalTime.of(9, 0)

                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        dueTime = LocalTime.of(hour, minute)
                                    },
                                    base.hour,
                                    base.minute,
                                    true
                                ).show()
                            }
                        )
                    }
                }

                // PRIORIDAD
                SectionTitle(stringResource(R.string.prioridad))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("low", "medium", "high").forEach { value ->
                        FilterChip(
                            selected = priority == value,
                            onClick = { priority = value },
                            label = { Text(priorityLabel(value)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LumiColors.Yellow,
                                selectedLabelColor = LumiColors.Dark,
                                containerColor = LumiColors.White,
                                labelColor = LumiColors.Dark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = priority == value,
                                borderColor = LumiColors.WarmGray,
                                selectedBorderColor = LumiColors.YellowStrong
                            )
                        )
                    }
                }

                // SUBTAREAS
                SectionTitle(stringResource(R.string.subtareas))

                subtasks.forEachIndexed { index, subtask ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LumiColors.White),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subtask.completed,
                            onCheckedChange = { checked ->
                                subtasks[index] = subtask.copy(completed = checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LumiColors.Yellow,
                                checkmarkColor = LumiColors.Dark,
                                uncheckedColor = LumiColors.Dark
                            )
                        )

                        Text(
                            text = subtask.title,
                            modifier = Modifier.weight(1f),
                            color = LumiColors.Dark
                        )

                        IconButton(onClick = { subtasks.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.quitar_subtarea),
                                tint = LumiColors.Dark
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LumiTextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        placeholder = stringResource(R.string.agregar_subtarea),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { addSubtask() },
                        enabled = newSubtask.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.agregar_subtarea),
                            tint = LumiColors.Dark
                        )
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Si había texto sin agregar en "Agregar subtarea", se incluye al guardar.
            LumiButton(
                text = when {
                    isLoading -> "Guardando..."
                    isEditing -> stringResource(R.string.guardar_cambios)
                    else -> stringResource(R.string.guardar_tarea)
                },
                enabled = title.isNotBlank() && !isLoading,
                onClick = {
                    val pending = newSubtask.trim()

                    val finalSubtasks = if (pending.isNotEmpty()) {
                        subtasks.toList() + SubtaskDraft(UUID.randomUUID().toString(), pending)
                    } else {
                        subtasks.toList()
                    }

                    onSave(
                        TaskForm(
                            title = title,
                            description = description,
                            subjectId = subjectId,
                            dueDate = dueDate,
                            dueTime = dueTime,
                            allDay = allDay,
                            priority = priority,
                            subtasks = finalSubtasks
                        )
                    )
                }
            )

            if (isEditing) {
                TextButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.eliminar_tarea),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.eliminar_tarea_2),
            message = stringResource(R.string.task_delete_message, initial?.title ?: ""),
            isLoading = isLoading,
            errorMessage = errorMessage,
            onConfirm = onDelete,
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = LumiColors.Dark
    )
}

@Composable
private fun PickerButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LumiColors.White)
            .border(1.dp, LumiColors.WarmGray, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = text, color = LumiColors.Dark)
    }
}

@Composable
private fun taskFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LumiColors.White,
    unfocusedContainerColor = LumiColors.White,
    disabledContainerColor = LumiColors.White,
    focusedBorderColor = LumiColors.YellowStrong,
    unfocusedBorderColor = LumiColors.WarmGray,
    focusedLabelColor = LumiColors.Dark,
    cursorColor = LumiColors.YellowStrong,
    focusedTextColor = LumiColors.Dark,
    unfocusedTextColor = LumiColors.Dark
)
