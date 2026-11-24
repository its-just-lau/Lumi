package com.lumi.app.features.tasks

import com.lumi.app.core.components.LumiIcon
import com.lumi.app.mascot.MascotCelebration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.lumi.app.core.components.LumiIconKind
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.core.components.LumiButton
import com.lumi.app.features.tasks.components.TaskCard
import com.lumi.app.features.tasks.components.TaskFormScreen
import com.lumi.app.core.components.SkeletonList
import com.lumi.app.ui.theme.LumiColors
import java.time.LocalDateTime
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import androidx.compose.foundation.layout.aspectRatio
import com.lumi.app.core.i18n.AppText
import com.lumi.app.mascot.LumiAnimations
import com.lumi.app.mascot.MascotAnimation
import com.lumi.app.mascot.MascotState

@Composable
fun TasksScreen(
    onNavigate: (String) -> Unit
) {
    val container = LumiApp.container

    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            container.taskRepository,
            container.subjectRepository
        )
    )

    val uiState by taskViewModel.uiState.collectAsState()
    val detailState by taskViewModel.detailState.collectAsState()
    val operationState by taskViewModel.operationState.collectAsState()
    val message by taskViewModel.message.collectAsState()
    val completedEvent by taskViewModel.completedEvent.collectAsState()

    val context = LocalContext.current

    var selectedFilter by remember {
        mutableStateOf(TaskFilter.ALL)
    }

    // Formulario a pantalla completa: crear (creating) o editar (editingTaskId)
    var creating by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }

    val subjects = (uiState as? TasksUiState.Success)?.subjects ?: emptyList()

    fun closeForm() {
        creating = false
        editingTaskId = null
        taskViewModel.resetOperationState()
        taskViewModel.resetDetailState()
    }

    LaunchedEffect(Unit) {
        taskViewModel.loadTasks()
    }

    // Al guardar o eliminar con éxito, se cierra el formulario
    LaunchedEffect(operationState) {
        if (operationState is TaskOperationState.Success) {
            closeForm()
        }
    }

    // Mensajes breves (recompensa al completar, errores al completar/reabrir)
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            taskViewModel.consumeMessage()
        }
    }

    // Celebración de Lumi al completar una tarea
    if (completedEvent) {
        MascotCelebration(
            animationRes = LumiAnimations.TASK_DONE,
            title = stringResource(R.string.tarea_completada),
            onDismiss = { taskViewModel.consumeCompleted() }
        )
    }

    // ----- FORMULARIO -----

    if (creating) {
        TaskFormScreen(
            initial = null,
            subjects = subjects,
            operationState = operationState,
            onSave = { form -> taskViewModel.saveTask(null, form) },
            onDelete = {},
            onBack = { closeForm() }
        )
        return
    }

    val editingId = editingTaskId

    if (editingId != null) {
        val detail = detailState

        if (detail is TaskDetailState.Success && detail.task.id == editingId) {
            TaskFormScreen(
                initial = detail.task,
                subjects = subjects,
                operationState = operationState,
                onSave = { form -> taskViewModel.saveTask(editingId, form) },
                onDelete = { taskViewModel.deleteTask(editingId) },
                onBack = { closeForm() }
            )
        } else {
            Scaffold(containerColor = LumiColors.Cream) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (detail is TaskDetailState.Error) {
                        MascotState(animationRes = LumiAnimations.forError(detail.message))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = detail.message,
                            fontSize = 15.sp,
                            color = LumiColors.Dark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LumiButton(text = stringResource(R.string.volver), onClick = { closeForm() })
                    } else {
                        MascotState(
                            animationRes = LumiAnimations.LOADING,
                            message = stringResource(R.string.mascot_loading)
                        )
                    }
                }
            }
        }
        return
    }

    // ----- LISTA -----

    val now = remember(uiState, selectedFilter) { LocalDateTime.now() }

    Scaffold(
        containerColor = LumiColors.Cream,

        bottomBar = {
            LumiBottomBar(
                selectedRoute = "tasks",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {

            // ENCABEZADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = stringResource(R.string.mis_tareas),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    modifier = Modifier.weight(1f)
                )

                MascotAnimation(
                    resId = LumiAnimations.GLANCE,
                    modifier = Modifier
                        .height(72.dp)
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                LumiIcon(
                    LumiIconKind.AddTask,
                    size = 46.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            taskViewModel.resetOperationState()
                            creating = true
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FILTROS
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(TaskFilter.all) { filter ->

                    FilterChip(
                        selected = selectedFilter == filter,

                        onClick = {
                            selectedFilter = filter
                        },

                        label = {
                            Text(
                                when (filter) {
                                    TaskFilter.TODAY -> stringResource(R.string.filter_today)
                                    TaskFilter.UPCOMING -> stringResource(R.string.filter_upcoming)
                                    TaskFilter.OVERDUE -> stringResource(R.string.filter_overdue)
                                    TaskFilter.COMPLETED -> stringResource(R.string.filter_completed)
                                    else -> stringResource(R.string.filter_all)
                                }
                            )
                        },

                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LumiColors.Yellow,
                            selectedLabelColor = LumiColors.Dark,
                            containerColor = LumiColors.White,
                            labelColor = LumiColors.Dark
                        ),

                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = LumiColors.WarmGray,
                            selectedBorderColor = LumiColors.YellowStrong
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CONTENIDO
            Text(
                text = when (selectedFilter) {
                    TaskFilter.TODAY -> stringResource(R.string.para_hoy)
                    TaskFilter.UPCOMING -> stringResource(R.string.proximas_tareas)
                    TaskFilter.OVERDUE -> stringResource(R.string.tareas_vencidas)
                    TaskFilter.COMPLETED -> stringResource(R.string.tareas_completadas)
                    else -> stringResource(R.string.todas_tus_tareas)
                },
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (val state = uiState) {

                TasksUiState.Idle,
                TasksUiState.Loading -> {
                    SkeletonList(count = 5)
                }

                is TasksUiState.Error -> {
                    MascotState(
                        animationRes = LumiAnimations.forError(state.message),
                        message = state.message
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        LumiButton(
                            text = stringResource(R.string.reintentar),
                            onClick = { taskViewModel.loadTasks() }
                        )
                    }
                }

                is TasksUiState.Success -> {
                    val visibleTasks = state.tasks.applyTaskFilter(selectedFilter, now)

                    if (visibleTasks.isEmpty()) {
                        EmptyTasksCard(filter = selectedFilter, hasTasks = state.tasks.isNotEmpty())
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(visibleTasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    subjectName = state.subjects
                                        .firstOrNull { it.id == task.subjectId }
                                        ?.name,
                                    now = now,
                                    onToggleComplete = { taskViewModel.toggleComplete(task) },
                                    onClick = {
                                        taskViewModel.resetOperationState()
                                        editingTaskId = task.id
                                        taskViewModel.loadTask(task.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksCard(
    filter: String,
    hasTasks: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = LumiColors.White
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            MascotAnimation(
                resId = LumiAnimations.EMPTY,
                modifier = Modifier
                    .height(190.dp)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.todo_listo_para_organizarte),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (hasTasks && filter != TaskFilter.ALL) {
                    AppText.get(R.string.no_hay_tareas_en_esta)
                } else {
                    AppText.get(R.string.aqui_apareceran_tus_tareas_agrega)
                },
                fontSize = 14.sp,
                color = LumiColors.Dark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
