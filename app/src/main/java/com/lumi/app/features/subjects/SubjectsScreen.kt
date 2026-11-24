package com.lumi.app.features.subjects

import com.lumi.app.core.components.LumiIcon
import androidx.compose.ui.draw.clip
import com.lumi.app.core.components.LumiIconKind
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.core.components.LumiButton
import com.lumi.app.core.components.LumiTextField
import com.lumi.app.features.subjects.components.EmptySubjectsState
import com.lumi.app.features.subjects.components.SemesterSelector
import com.lumi.app.core.components.SkeletonGrid
import com.lumi.app.features.subjects.components.SubjectCard
import com.lumi.app.features.subjects.model.Subject
import com.lumi.app.features.subjects.model.Semester
import com.lumi.app.ui.theme.LumiColors
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.data.repositories.SubjectDependencies
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.lumi.app.features.subjects.SubjectViewModel
import com.lumi.app.features.subjects.SubjectViewModelFactory
import com.lumi.app.features.subjects.SubjectsUiState
import com.lumi.app.features.subjects.components.ConfirmDeleteDialog
import com.lumi.app.features.subjects.components.SemesterManagerDialog
import com.lumi.app.features.subjects.mapper.toSemester
import com.lumi.app.features.subjects.mapper.toSubject
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

@Composable
fun SubjectsScreen(
    onNavigate: (String) -> Unit
) {
    // ViewModel conectado con Retrofit
    val subjectViewModel: SubjectViewModel = viewModel(
        factory = SubjectViewModelFactory(SubjectDependencies.repository)
    )

    val uiState by subjectViewModel.uiState.collectAsState()

    val creationState by subjectViewModel.creationState.collectAsState()

    val detailState by subjectViewModel.detailState.collectAsState()

    val operationState by subjectViewModel.operationState.collectAsState()

    val semesterOperationState by subjectViewModel.semesterOperationState.collectAsState()

    val context = LocalContext.current

    // Cargar materias y semestres al entrar a la pantalla
    LaunchedEffect(Unit) {
        subjectViewModel.loadSubjects()
    }

    // Convertir las respuestas de la API a los modelos de la interfaz
    val subjects = (uiState as? SubjectsUiState.Success)
        ?.subjects
        ?.filter { it.status == "active" }
        ?.map { it.toSubject() } ?: emptyList()

    // Semestres del más reciente al más antiguo (el primero se muestra como "actual")
    val semesters = (uiState as? SubjectsUiState.Success)
        ?.semesters
        ?.map { it.toSemester() }
        ?.sortedWith(
            compareByDescending<Semester> { it.startDate ?: "" }
                .thenByDescending { it.name }
        )
        ?.map { semester ->
            semester.copy(
                subjectCount = subjects.count { it.semesterId == semester.id }
            )
        } ?: emptyList()

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf(AppText.get(R.string.todos))
    }

    // Estado del formulario de nueva materia
    var showAddSubjectDialog by remember {
        mutableStateOf(false)
    }

    var subjectName by remember {
        mutableStateOf("")
    }

    var subjectDescription by remember {
        mutableStateOf("")
    }

    var selectedColor by remember {
        mutableStateOf("#FFD83D")
    }

    var selectedSemesterId by remember {
        mutableStateOf<String?>(null)
    }

    var semesterMenuExpanded by remember {
        mutableStateOf(false)
    }

    // Edición y eliminación de materias
    var editingSubjectId by remember {
        mutableStateOf<String?>(null)
    }

    var showDeleteSubjectDialog by remember {
        mutableStateOf(false)
    }

    // Administración de semestres
    var showSemesterManager by remember {
        mutableStateOf(false)
    }

// Colores disponibles para las tarjetas
    val subjectColors = listOf(
        "#FFD83D",
        "#FFB8A2",
        "#B9D9FF",
        "#BCE6C5",
        "#D8C5F5",
        "#FFC9DF"
    )

    val allLabel = AppText.get(R.string.todos)
    val independentLabel = AppText.get(R.string.independientes)

    val filters = listOf(allLabel) +
            semesters.map { it.name } +
            independentLabel

    // Si el semestre seleccionado se renombra o elimina, vuelve a "Todos"
    LaunchedEffect(filters) {
        if (selectedFilter !in filters) {
            selectedFilter = allLabel
        }
    }

    val activeSubjects = subjects.filter {
        !it.isDraft
    }

    val draftSubjects = subjects.filter {
        it.isDraft
    }

    val filteredSubjects = activeSubjects.filter { subject ->

        val matchesSearch = subject.name.contains(
            searchText,
            ignoreCase = true
        )

        val matchesFilter = when (selectedFilter) {
            allLabel -> true

            independentLabel -> {
                subject.semesterId == null
            }

            else -> {
                semesters.any {
                    it.name == selectedFilter &&
                            it.id == subject.semesterId
                }
            }
        }

        matchesSearch && matchesFilter
    }

    LaunchedEffect(creationState) {
        when (creationState) {
            SubjectCreationState.Success -> {
                showAddSubjectDialog = false
                subjectName = ""
                subjectDescription = ""
                selectedColor = "#FFD83D"
                selectedSemesterId = null
                subjectViewModel.resetCreationState()
            }

            else -> Unit
        }
    }

    // Al consultar una materia para editarla, se abre el formulario con sus datos
    LaunchedEffect(detailState) {
        val detail = detailState

        if (detail is SubjectDetailState.Success &&
            detail.subject.id == editingSubjectId &&
            !showAddSubjectDialog
        ) {
            subjectName = detail.subject.name
            subjectDescription = detail.subject.description.orEmpty()
            selectedColor = detail.subject.color ?: "#FFD83D"
            selectedSemesterId = detail.subject.semesterId
            showAddSubjectDialog = true
        }

        if (detail is SubjectDetailState.Error && editingSubjectId != null) {
            Toast.makeText(context, detail.message, Toast.LENGTH_SHORT).show()
            editingSubjectId = null
            subjectViewModel.resetDetailState()
        }
    }

    // Al guardar o eliminar con éxito, se cierran los formularios
    LaunchedEffect(operationState) {
        if (operationState is SubjectOperationState.Success) {
            showAddSubjectDialog = false
            showDeleteSubjectDialog = false
            editingSubjectId = null
            subjectName = ""
            subjectDescription = ""
            selectedColor = "#FFD83D"
            selectedSemesterId = null
            subjectViewModel.resetOperationState()
        }
    }

    Scaffold(
        containerColor = LumiColors.Cream,
        bottomBar = {
            LumiBottomBar(
                selectedRoute = "subjects",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            // ENCABEZADO PRINCIPAL
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.mis_materias),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                LumiIcon(
                    LumiIconKind.AddSubject,
                    size = 46.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showAddSubjectDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BUSCADOR
            LumiTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                placeholder = stringResource(R.string.buscar_materia),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.buscar),
                        tint = LumiColors.Dark
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SELECTOR DE SEMESTRES
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SemesterSelector(
                        filters = filters,
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            selectedFilter = it
                        }
                    )
                }

                IconButton(
                    onClick = {
                        showSemesterManager = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.administrar_semestres),
                        tint = LumiColors.Dark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONTENIDO DE MATERIAS
            when (val state = uiState) {

                SubjectsUiState.Idle,
                SubjectsUiState.Loading -> {
                    SkeletonGrid(rows = 3)
                }

                is SubjectsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_pudimos_cargar_tus_materias),
                            color = LumiColors.Dark,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.message,
                            color = LumiColors.Dark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LumiButton(
                            text = stringResource(R.string.reintentar),
                            onClick = {
                                subjectViewModel.loadSubjects()
                            }
                        )
                    }
                }

                is SubjectsUiState.Success -> {

                    if (activeSubjects.isEmpty()) {
                        EmptySubjectsState(
                            onAddSubject = {
                                showAddSubjectDialog = true
                            }
                        )
                    } else {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {

                            // MATERIAS ACTIVAS
                            item(
                                span = {
                                    GridItemSpan(2)
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.materias_activas),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LumiColors.Dark,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            if (filteredSubjects.isEmpty()) {
                                item(
                                    span = {
                                        GridItemSpan(2)
                                    }
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_encontramos_materias_en_esta),
                                        color = LumiColors.Dark,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            } else {
                                items(
                                    items = filteredSubjects,
                                    key = { it.id }
                                ) { subject ->

                                    SubjectCard(
                                        subject = subject,
                                        semesterName = semesters
                                            .firstOrNull {
                                                it.id == subject.semesterId
                                            }
                                            ?.name,
                                        onClick = {
                                            editingSubjectId = subject.id
                                            subjectViewModel.loadSubject(subject.id)
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

    val closeSubjectForm = {
        showAddSubjectDialog = false
        editingSubjectId = null
        subjectName = ""
        subjectDescription = ""
        selectedColor = "#FFD83D"
        selectedSemesterId = null
        subjectViewModel.resetDetailState()
        subjectViewModel.resetOperationState()
        subjectViewModel.resetCreationState()
    }

// FORMULARIO PARA AGREGAR O EDITAR MATERIA
    if (showAddSubjectDialog) {

        AlertDialog(
            onDismissRequest = {
                closeSubjectForm()
            },
            title = {
                Text(
                    text = if (editingSubjectId == null) stringResource(R.string.nueva_materia) else stringResource(R.string.editar_materia),
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Nombre
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text(stringResource(R.string.nombre_de_la_materia)) },
                        placeholder = {
                            Text(stringResource(R.string.ej_programacion))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Descripción
                    OutlinedTextField(
                        value = subjectDescription,
                        onValueChange = { subjectDescription = it },
                        label = { Text(stringResource(R.string.descripcion)) },
                        placeholder = {
                            Text(stringResource(R.string.describe_brevemente_la_materia))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    // Selector de color
                    Text(
                        text = stringResource(R.string.color_de_la_tarjeta),
                        fontWeight = FontWeight.SemiBold,
                        color = LumiColors.Dark
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        subjectColors.forEach { colorHex ->

                            val colorValue = Color(
                                android.graphics.Color.parseColor(colorHex)
                            )

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        color = colorValue,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColor = colorHex
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == colorHex) {
                                    Text(
                                        text = "✓",
                                        color = LumiColors.Dark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Selector de semestre
                    Text(
                        text = stringResource(R.string.semestre),
                        fontWeight = FontWeight.SemiBold,
                        color = LumiColors.Dark
                    )

                    Box {
                        OutlinedButton(
                            onClick = {
                                semesterMenuExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val selectedSemester = semesters.firstOrNull {
                                it.id == selectedSemesterId
                            }

                            Text(
                                text = selectedSemester?.name
                                    ?: stringResource(R.string.sin_semestre)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = semesterMenuExpanded,
                            onDismissRequest = {
                                semesterMenuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.sin_semestre))
                                },
                                onClick = {
                                    selectedSemesterId = null
                                    semesterMenuExpanded = false
                                }
                            )

                            semesters.forEach { semester ->
                                DropdownMenuItem(
                                    text = {
                                        Text(semester.name)
                                    },
                                    onClick = {
                                        selectedSemesterId = semester.id
                                        semesterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (creationState is SubjectCreationState.Error) {
                        Text(
                            text = (creationState as SubjectCreationState.Error).message,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }

                    if (operationState is SubjectOperationState.Error) {
                        Text(
                            text = (operationState as SubjectOperationState.Error).message,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }

                    if (editingSubjectId != null) {
                        TextButton(
                            onClick = {
                                showDeleteSubjectDialog = true
                            }
                        ) {
                            Text(stringResource(R.string.eliminar_materia), color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val editingId = editingSubjectId

                        if (editingId == null) {
                            subjectViewModel.createSubject(
                                name = subjectName,
                                description = subjectDescription,
                                color = selectedColor,
                                semesterId = selectedSemesterId
                            )
                        } else {
                            subjectViewModel.updateSubject(
                                subjectId = editingId,
                                name = subjectName,
                                description = subjectDescription,
                                color = selectedColor,
                                semesterId = selectedSemesterId
                            )
                        }
                    },
                    enabled = creationState !is SubjectCreationState.Loading
                            && operationState !is SubjectOperationState.Loading
                            && subjectName.isNotBlank()
                ) {
                    Text(
                        if (creationState is SubjectCreationState.Loading ||
                            operationState is SubjectOperationState.Loading
                        ) {
                            "Guardando..."
                        } else {
                            AppText.get(R.string.guardar)
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        closeSubjectForm()
                    }
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            containerColor = LumiColors.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // CONFIRMAR ELIMINACIÓN DE MATERIA
    if (showDeleteSubjectDialog) {
        val subjectToDelete = subjects.firstOrNull {
            it.id == editingSubjectId
        }

        ConfirmDeleteDialog(
            title = stringResource(R.string.eliminar_materia_2),
            message = stringResource(R.string.se_eliminara_esta_accion_no, subjectToDelete?.name ?: subjectName),
            isLoading = operationState is SubjectOperationState.Loading,
            errorMessage = (operationState as? SubjectOperationState.Error)?.message,
            onConfirm = {
                editingSubjectId?.let { subjectViewModel.deleteSubject(it) }
            },
            onDismiss = {
                showDeleteSubjectDialog = false
                subjectViewModel.resetOperationState()
            }
        )
    }

    // ADMINISTRAR SEMESTRES
    if (showSemesterManager) {
        SemesterManagerDialog(
            semesters = semesters,
            operationState = semesterOperationState,
            onCreate = { name -> subjectViewModel.createSemester(name) },
            onRename = { id, name -> subjectViewModel.renameSemester(id, name) },
            onDelete = { id -> subjectViewModel.deleteSemester(id) },
            onOperationHandled = { subjectViewModel.resetSemesterOperationState() },
            onDismiss = { showSemesterManager = false }
        )
    }

}
