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

@Composable
fun SubjectsScreen(
    onNavigate: (String) -> Unit
) {
    val semesters = remember {
        listOf(
            Semester(1, "7.º semestre"),
            Semester(2, "6.º semestre"),
            Semester(3, "5.º semestre")
        )
    }

    var subjects by remember {
        mutableStateOf(
            listOf(
                Subject(
                    1, "Programación", 1,
                    color = 0xFFFFE6A3,
                    progress = 65
                ),
                Subject(
                    2, "Bases de datos", 1,
                    color = 0xFFDCE7FF,
                    progress = 40
                ),
                Subject(
                    3, "Matemáticas", 1,
                    color = 0xFFFFDFDF,
                    progress = 20
                ),
                Subject(
                    4, "Redes", 1,
                    color = 0xFFDDF1DF,
                    progress = 80
                ),
                Subject(
                    5, "Inglés",
                    color = 0xFFE9DFFF,
                    progress = 30
                ),
                Subject(
                    6, "Diseño UX/UI",
                    color = 0xFFFFE3CF,
                    progress = 15
                ),
                Subject(
                    7, "Nueva materia", 1,
                    color = 0xFFFFE6A3,
                    isDraft = true
                )
            )
        )
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("7.º semestre")
    }

    val filters = listOf(
        "Todos",
        "7.º semestre",
        "6.º semestre",
        "5.º semestre",
        "Independientes"
    )

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
                        // Próximamente: registro de materia
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
            if (activeSubjects.isEmpty() && draftSubjects.isEmpty()) {

                EmptySubjectsState(
                    onAddSubject = {
                        // Próximamente: registro de materia
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
                            modifier = Modifier.padding(
                                bottom = 4.dp
                            )
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
                                modifier = Modifier.padding(
                                    vertical = 12.dp
                                )
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

                    // BORRADORES
                    if (draftSubjects.isNotEmpty()) {

                        item(
                            span = {
                                GridItemSpan(2)
                            }
                        ) {
                            Text(
                                text = "Borradores (${draftSubjects.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark,
                                modifier = Modifier.padding(
                                    top = 12.dp,
                                    bottom = 4.dp
                                )
                            )
                        }

                        items(
                            items = draftSubjects,
                            key = { it.id }
                        ) { subject ->

                            SubjectCard(
                                subject = subject,
                                isDraft = true
                            )
                        }
                    }
                }
            }
        }
    }
}
