package com.lumi.app.features.subjects.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.features.subjects.SemesterOperationState
import com.lumi.app.features.subjects.model.Semester
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

/**
 * Hoja inferior para administrar semestres.
 * Separa el semestre actual de los anteriores y permite crear, renombrar y eliminar.
 * El semestre actual es el primero de la lista recibida (la pantalla ya los ordena
 * del más reciente al más antiguo).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterManagerDialog(
    semesters: List<Semester>,
    operationState: SemesterOperationState,
    onCreate: (String) -> Unit,
    onRename: (semesterId: String, name: String) -> Unit,
    onDelete: (semesterId: String) -> Unit,
    onOperationHandled: () -> Unit,
    onDismiss: () -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var nameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Semester?>(null) }

    val isLoading = operationState is SemesterOperationState.Loading
    val errorMessage = (operationState as? SemesterOperationState.Error)?.message

    // Al terminar bien una operación, se cierran los formularios internos.
    LaunchedEffect(operationState) {
        if (operationState is SemesterOperationState.Success) {
            showForm = false
            editingId = null
            nameText = ""
            deleteTarget = null
            onOperationHandled()
        }
    }

    val closeSheet = {
        onOperationHandled()
        onDismiss()
    }

    val currentSemester = semesters.firstOrNull()
    val previousSemesters = semesters.drop(1)
    val hasBlockedDelete = semesters.any { it.subjectCount > 0 }

    ModalBottomSheet(
        onDismissRequest = closeSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = LumiColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.mis_semestres),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LumiColors.Dark
                    )
                    Text(
                        text = stringResource(R.string.organiza_tus_materias_por_periodo),
                        fontSize = 13.sp,
                        color = LumiColors.Dark.copy(alpha = 0.6f)
                    )
                }

                IconButton(onClick = closeSheet) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cerrar),
                        tint = LumiColors.Dark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentSemester == null) {
                    Text(
                        text = stringResource(R.string.aun_no_tienes_semestres_crea),
                        fontSize = 14.sp,
                        color = LumiColors.Dark
                    )
                } else {
                    SectionLabel("ACTUAL")

                    SemesterRow(
                        semester = currentSemester,
                        highlighted = true,
                        onEdit = {
                            editingId = currentSemester.id
                            nameText = currentSemester.name
                            onOperationHandled()
                            showForm = true
                        },
                        onDelete = {
                            onOperationHandled()
                            deleteTarget = currentSemester
                        }
                    )

                    if (previousSemesters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionLabel("ANTERIORES")

                        previousSemesters.forEach { semester ->
                            SemesterRow(
                                semester = semester,
                                highlighted = false,
                                onEdit = {
                                    editingId = semester.id
                                    nameText = semester.name
                                    onOperationHandled()
                                    showForm = true
                                },
                                onDelete = {
                                    onOperationHandled()
                                    deleteTarget = semester
                                }
                            )
                        }
                    }
                }

                if (hasBlockedDelete) {
                    Text(
                        text = stringResource(R.string.para_eliminar_un_semestre_primero),
                        fontSize = 12.sp,
                        color = LumiColors.Dark.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (!showForm && deleteTarget == null && !errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LumiButton(
                text = stringResource(R.string.nuevo_semestre),
                onClick = {
                    editingId = null
                    nameText = ""
                    onOperationHandled()
                    showForm = true
                }
            )
        }
    }

    // Formulario de nombre (nuevo o edición)
    if (showForm) {
        AlertDialog(
            onDismissRequest = {
                showForm = false
                editingId = null
                nameText = ""
                onOperationHandled()
            },
            title = {
                Text(
                    text = if (editingId == null) stringResource(R.string.nuevo_semestre) else stringResource(R.string.editar_semestre),
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text(stringResource(R.string.nombre_del_semestre)) },
                        placeholder = { Text(stringResource(R.string.ej_2026_1)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = editingId

                        if (id == null) {
                            onCreate(nameText)
                        } else {
                            onRename(id, nameText)
                        }
                    },
                    enabled = nameText.isNotBlank() && !isLoading
                ) {
                    Text(if (isLoading) "Guardando..." else stringResource(R.string.guardar))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForm = false
                        editingId = null
                        nameText = ""
                        onOperationHandled()
                    }
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            containerColor = LumiColors.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Confirmación para eliminar
    deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.eliminar_semestre),
            message = stringResource(R.string.se_eliminara_esta_accion_no, target.name),
            isLoading = isLoading,
            errorMessage = errorMessage,
            onConfirm = { onDelete(target.id) },
            onDismiss = {
                deleteTarget = null
                onOperationHandled()
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LumiColors.Dark.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SemesterRow(
    semester: Semester,
    highlighted: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val canDelete = semester.subjectCount == 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (highlighted) LumiColors.Yellow else LumiColors.Cream)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = semester.name,
                fontSize = if (highlighted) 20.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LumiColors.Dark
            )
            Text(
                text = when (semester.subjectCount) {
                    0 -> stringResource(R.string.sin_materias)
                    1 -> stringResource(R.string.s_1_materia)
                    else -> stringResource(R.string.materias_2, semester.subjectCount)
                },
                fontSize = 13.sp,
                color = LumiColors.Dark.copy(alpha = 0.7f)
            )
        }

        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.editar_semestre_2, semester.name),
                tint = LumiColors.Dark
            )
        }

        IconButton(
            onClick = onDelete,
            enabled = canDelete,
            modifier = Modifier.alpha(if (canDelete) 1f else 0.35f)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = if (canDelete) {
                    AppText.get(R.string.eliminar_semestre_2, semester.name)
                } else {
                    AppText.get(R.string.no_se_puede_eliminar_tiene, semester.name)
                },
                tint = LumiColors.Dark
            )
        }
    }
}
