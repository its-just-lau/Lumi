package com.lumi.app.features.settings

import android.content.Context
import com.lumi.app.mascot.LumiAnimations
import com.lumi.app.mascot.MascotImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

private const val PREFS_NAME = "lumi_notifications"

/**
 * Los avisos se guardan solo en el dispositivo: el contrato de la API aún no tiene
 * endpoints de notificaciones. Cuando existan, aquí se envía el cambio al servidor.
 */
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    SettingsScaffold(title = stringResource(R.string.notificaciones), onBack = onBack) {
        MascotImage(LumiAnimations.SLEEPING, 150.dp, centered = true)
        SettingsSectionTitle(stringResource(R.string.avisos))
        SettingsGroup {
            NotificationSwitch(
                title = stringResource(R.string.recordatorio_de_estudio),
                subtitle = stringResource(R.string.un_aviso_diario_para_sentarte),
                prefKey = "study_reminder",
                prefs = prefs,
                showDivider = true
            )
            NotificationSwitch(
                title = stringResource(R.string.tareas_por_vencer),
                subtitle = stringResource(R.string.te_avisamos_antes_de_la),
                prefKey = "task_reminder",
                prefs = prefs,
                showDivider = true
            )
            NotificationSwitch(
                title = stringResource(R.string.racha_en_peligro),
                subtitle = stringResource(R.string.si_aun_no_estudias_hoy),
                prefKey = "streak_reminder",
                prefs = prefs,
                showDivider = false
            )
        }

        Text(
            text = stringResource(R.string.notifications_footer),
            modifier = Modifier.padding(horizontal = 4.dp),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = LumiColors.Dark.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun NotificationSwitch(
    title: String,
    subtitle: String,
    prefKey: String,
    prefs: android.content.SharedPreferences,
    showDivider: Boolean
) {
    var checked by remember { mutableStateOf(prefs.getBoolean(prefKey, true)) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LumiColors.Dark
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = LumiColors.Dark.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    prefs.edit().putBoolean(prefKey, it).apply()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LumiColors.Dark,
                    checkedTrackColor = LumiColors.Yellow,
                    uncheckedThumbColor = LumiColors.White,
                    uncheckedTrackColor = LumiColors.WarmGray
                )
            )
        }
        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(color = LumiColors.WarmGray)
        }
    }
}
