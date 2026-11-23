package com.lumi.app.features.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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

    // Cargar materias y semestres al entrar a la pantalla
    LaunchedEffect(Unit) {
        subjectViewModel.loadSubjects()
    }

    // Convertir las respuestas de la API a los modelos de la interfaz
    val semesters = (uiState as? SubjectsUiState.Success)
        ?.semesters
        ?.map { semesterResponse ->
            Semester(
                id = semesterResponse.id,
                name = semesterResponse.name
            )
        } ?: emptyList()

    val subjects = (uiState as? SubjectsUiState.Success)
        ?.subjects
        ?.filter { it.status == "active" }
        ?.map { subjectResponse ->
            Subject(
                id = subjectResponse.id,
                name = subjectResponse.name,
                semesterId = subjectResponse.semesterId,
                description = subjectResponse.description,
                color = subjectResponse.color
                    ?.removePrefix("#")
                    ?.let { hex ->
                        when (hex.length) {
                            6 -> ("FF$hex").toLongOrNull(16)
                            8 -> hex.toLongOrNull(16)
                            else -> null
                        }
                    } ?: 0xFFFFE6A3
            )
        } ?: emptyList()

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("Todos")
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

// Colores disponibles para las tarjetas
    val subjectColors = listOf(
        "#FFD83D",
        "#FFB8A2",
        "#B9D9FF",
        "#BCE6C5",
        "#D8C5F5",
        "#FFC9DF"
    )

    val filters = listOf("Todos") +
            semesters.map { it.name } +
            "Independientes"

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
            "Todos" -> true

            "Independientes" -> {
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
                    text = "Mis materias",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                LumiButton(
                    text = "+",
                    onClick = {
                        showAddSubjectDialog = true
                    },
                    modifier = Modifier.width(55.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BUSCADOR
            LumiTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                placeholder = "Buscar materia...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = LumiColors.Dark
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SELECTOR DE SEMESTRES
            SemesterSelector(
                filters = filters,
                selectedFilter = selectedFilter,
                onFilterSelected = {
                    selectedFilter = it
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CONTENIDO DE MATERIAS
            when (val state = uiState) {

                SubjectsUiState.Idle,
                SubjectsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = LumiColors.YellowStrong
                        )
                    }
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
                            text = "No pudimos cargar tus materias",
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
                            text = "Reintentar",
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
                                    text = "Materias activas",
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
                                        text = "No encontramos materias en esta selección.",
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
                                            ?.name
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

// FORMULARIO PARA AGREGAR MATERIA
    if (showAddSubjectDialog) {

        AlertDialog(
            onDismissRequest = {
                showAddSubjectDialog = false
            },
            title = {
                Text(
                    text = "Nueva materia",
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
                        label = { Text("Nombre de la materia") },
                        placeholder = {
                            Text("Ej. Programación")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Descripción
                    OutlinedTextField(
                        value = subjectDescription,
                        onValueChange = { subjectDescription = it },
                        label = { Text("Descripción") },
                        placeholder = {
                            Text("Describe brevemente la materia")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    // Selector de color
                    Text(
                        text = "Color de la tarjeta",
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
                        text = "Semestre",
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
                                    ?: "Sin semestre"
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
                                    Text("Sin semestre")
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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        subjectViewModel.createSubject(
                            name = subjectName,
                            description = subjectDescription,
                            color = selectedColor,
                            semesterId = selectedSemesterId
                        )
                    },
                    enabled = creationState !is SubjectCreationState.Loading
                            && subjectName.isNotBlank()
                ) {
                    Text(
                        if (creationState is SubjectCreationState.Loading) {
                            "Guardando..."
                        } else {
                            "Guardar"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddSubjectDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = LumiColors.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

}
