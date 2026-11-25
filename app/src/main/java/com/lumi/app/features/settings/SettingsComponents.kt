package com.lumi.app.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiTopBar
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

val DangerRed = Color(0xFFD64545)

/** Estructura común de las pantallas de configuración: barra con flecha atrás y contenido con scroll. */
@Composable
fun SettingsScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = LumiColors.Cream,
        topBar = {
            LumiTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
                            tint = LumiColors.Dark
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = LumiColors.Dark
    )
}

/** Tarjeta blanca que agrupa filas separadas por una línea fina. */
@Composable
fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White),
        border = BorderStroke(1.dp, LumiColors.WarmGray)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    color: Color = LumiColors.Dark
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LumiColors.Dark.copy(alpha = 0.5f)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = LumiColors.WarmGray)
        }
    }
}

@Composable
fun InfoDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LumiColors.White,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, color = LumiColors.Dark)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = LumiColors.Dark.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.entendido), color = LumiColors.YellowStrong)
            }
        }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    confirmColor: Color = LumiColors.YellowStrong,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LumiColors.White,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, color = LumiColors.Dark)
        },
        text = {
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = LumiColors.Dark.copy(alpha = 0.75f)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar), color = LumiColors.Dark)
            }
        }
    )
}

@Composable
fun SettingsSpacer(height: Int = 8) {
    Spacer(modifier = Modifier.height(height.dp))
}
