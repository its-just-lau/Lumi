package com.lumi.app.features.study.materials

import com.lumi.app.mascot.LumiAnimations
import com.lumi.app.mascot.MascotAnimation
import com.lumi.app.mascot.MascotState
import androidx.compose.foundation.layout.aspectRatio
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.core.components.LumiTextField
import com.lumi.app.data.models.learning.MaterialResponse
import com.lumi.app.features.subjects.components.ConfirmDeleteDialog
import com.lumi.app.core.components.SkeletonList
import com.lumi.app.ui.theme.LumiColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

private const val MAX_PDF_BYTES = 10 * 1024 * 1024

private class PickedPdf(val name: String, val bytes: ByteArray)

/**
 * Materiales de estudio: el usuario sube texto o un PDF y Lumi genera a partir de él
 * lecciones, tarjetas y cuestionarios (el servidor hace el procesamiento).
 */
@Composable
fun MaterialsScreen(
    onBack: () -> Unit
) {
    val viewModel: MaterialsViewModel = viewModel(
        factory = MaterialsViewModelFactory(LumiApp.container.materialRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val message by viewModel.message.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showForm by remember { mutableStateOf(false) }
    var useText by remember { mutableStateOf(true) }
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var pickedPdf by remember { mutableStateOf<PickedPdf?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<MaterialResponse?>(null) }

    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        viewModel.loadMaterials()
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    // Al guardar o eliminar con éxito se cierran los diálogos
    LaunchedEffect(operationState) {
        if (operationState is MaterialOperationState.Success) {
            showForm = false
            deleteTarget = null
            titleText = ""
            contentText = ""
            pickedPdf = null
            localError = null
            viewModel.resetOperationState()
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val pdf = withContext(Dispatchers.IO) { readPdf(context, uri) }

                when {
                    pdf == null -> localError = AppText.get(R.string.no_se_pudo_leer_el)
                    pdf.bytes.size > MAX_PDF_BYTES -> localError = AppText.get(R.string.el_pdf_no_debe_pesar)
                    else -> {
                        pickedPdf = pdf
                        localError = null

                        if (titleText.isBlank()) {
                            titleText = pdf.name.removeSuffix(".pdf").removeSuffix(".PDF")
                        }
                    }
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.volver),
                        tint = LumiColors.Dark
                    )
                }

                Text(
                    text = stringResource(R.string.mis_materiales),
                    modifier = Modifier.weight(1f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                LumiButton(
                    text = "+",
                    onClick = {
                        viewModel.resetOperationState()
                        localError = null
                        showForm = true
                    },
                    modifier = Modifier.width(55.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                MaterialsUiState.Idle,
                MaterialsUiState.Loading -> {
                    SkeletonList(count = 4)
                }

                is MaterialsUiState.Error -> {
                    MascotState(
                        animationRes = LumiAnimations.forError(state.message),
                        message = state.message
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        LumiButton(
                            text = stringResource(R.string.reintentar),
                            onClick = { viewModel.loadMaterials() }
                        )
                    }
                }

                is MaterialsUiState.Success -> {
                    if (state.materials.isEmpty()) {
                        EmptyMaterialsCard()
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.materials, key = { it.id }) { material ->
                                MaterialCard(
                                    material = material,
                                    progress = state.progress[material.id],
                                    onProcessAgain = { viewModel.processAgain(material) },
                                    onDelete = {
                                        viewModel.resetOperationState()
                                        deleteTarget = material
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // FORMULARIO DE NUEVO MATERIAL
    if (showForm) {
        val isLoading = operationState is MaterialOperationState.Loading
        val errorMessage = localError ?: (operationState as? MaterialOperationState.Error)?.message

        AlertDialog(
            onDismissRequest = {
                if (!isLoading) {
                    showForm = false
                    viewModel.resetOperationState()
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.nuevo_material),
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(true to AppText.get(R.string.texto), false to "PDF").forEach { (isText, label) ->
                            FilterChip(
                                selected = useText == isText,
                                onClick = {
                                    useText = isText
                                    localError = null
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LumiColors.Yellow,
                                    selectedLabelColor = LumiColors.Dark,
                                    containerColor = LumiColors.White,
                                    labelColor = LumiColors.Dark
                                )
                            )
                        }
                    }

                    LumiTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = stringResource(R.string.titulo)
                    )

                    if (useText) {
                        OutlinedTextField(
                            value = contentText,
                            onValueChange = { contentText = it },
                            label = { Text(stringResource(R.string.pega_aqui_tus_apuntes)) },
                            minLines = 5,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LumiColors.White,
                                unfocusedContainerColor = LumiColors.White,
                                focusedBorderColor = LumiColors.YellowStrong,
                                unfocusedBorderColor = LumiColors.WarmGray,
                                focusedLabelColor = LumiColors.Dark,
                                cursorColor = LumiColors.YellowStrong,
                                focusedTextColor = LumiColors.Dark,
                                unfocusedTextColor = LumiColors.Dark
                            )
                        )
                    } else {
                        LumiButton(
                            text = pickedPdf?.name ?: stringResource(R.string.elegir_pdf),
                            onClick = { pdfLauncher.launch(arrayOf("application/pdf")) },
                            backgroundColor = LumiColors.Cream
                        )

                        Text(
                            text = stringResource(R.string.maximo_10_mb),
                            fontSize = 12.sp,
                            color = LumiColors.Dark.copy(alpha = 0.6f)
                        )
                    }

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
                    enabled = !isLoading,
                    onClick = {
                        localError = null

                        if (useText) {
                            viewModel.createTextMaterial(titleText, contentText)
                        } else {
                            val pdf = pickedPdf

                            if (pdf == null) {
                                localError = AppText.get(R.string.elige_un_archivo_pdf)
                            } else {
                                viewModel.uploadPdf(titleText, pdf.name, pdf.bytes)
                            }
                        }
                    }
                ) {
                    Text(if (isLoading) "Guardando..." else stringResource(R.string.guardar))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        showForm = false
                        viewModel.resetOperationState()
                    }
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            containerColor = LumiColors.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.eliminar_material),
            message = stringResource(R.string.se_eliminara_y_el_contenido, target.title),
            isLoading = operationState is MaterialOperationState.Loading,
            errorMessage = (operationState as? MaterialOperationState.Error)?.message,
            onConfirm = { viewModel.deleteMaterial(target.id) },
            onDismiss = {
                deleteTarget = null
                viewModel.resetOperationState()
            }
        )
    }
}

@Composable
private fun MaterialCard(
    material: MaterialResponse,
    progress: Int?,
    onProcessAgain: () -> Unit,
    onDelete: () -> Unit
) {
    val statusText = when (material.status) {
        "completed" -> stringResource(R.string.listo)
        "failed" -> stringResource(R.string.fallo)
        "processing" -> if (progress != null && progress > 0) stringResource(R.string.procesando_2, progress) else stringResource(R.string.procesando)
        else -> stringResource(R.string.pendiente)
    }

    val statusColor = when (material.status) {
        "completed" -> Color(0xFFBCE6C5)
        "failed" -> Color(0xFFFFB8A2)
        "processing" -> LumiColors.Yellow
        else -> LumiColors.WarmGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (material.type == "pdf") "PDF" else stringResource(R.string.texto),
                        fontSize = 13.sp,
                        color = LumiColors.Dark.copy(alpha = 0.7f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )
                    }
                }
            }

            if (material.status == "failed" || material.status == "pending") {
                IconButton(onClick = onProcessAgain) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.procesar_de_nuevo),
                        tint = LumiColors.Dark
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.eliminar_material_2),
                    tint = LumiColors.Dark
                )
            }
        }
    }
}

@Composable
private fun EmptyMaterialsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
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
                text = stringResource(R.string.sube_tu_primer_material),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.pega_tus_apuntes_o_sube),
                fontSize = 14.sp,
                color = LumiColors.Dark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun readPdf(context: Context, uri: Uri): PickedPdf? {
    return try {
        val resolver = context.contentResolver
        var name = "material.pdf"

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (index >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(index) ?: name
            }
        }

        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

        PickedPdf(name, bytes)
    } catch (exception: Exception) {
        null
    }
}
