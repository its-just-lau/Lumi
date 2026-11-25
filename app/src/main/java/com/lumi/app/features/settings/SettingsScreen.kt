package com.lumi.app.features.settings

import android.content.Intent
import com.lumi.app.core.i18n.AppText
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

/** Correo al que llegan las sugerencias. Cámbialo por el del equipo. */
private const val SUPPORT_EMAIL = "soporte@lumi.app"

private enum class SettingsDialog {
    Help, Terms, Privacy, Credits, Logout, DeleteAccount
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AccountViewModel = viewModel(
        factory = AccountViewModelFactory(LumiApp.container.userRepository)
    )
    val state by viewModel.state.collectAsState()
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    SettingsScaffold(title = stringResource(R.string.configuracion), onBack = onBack) {

        SettingsSectionTitle(stringResource(R.string.cuenta))
        SettingsGroup {
            SettingsRow(stringResource(R.string.editar_perfil), onClick = { onNavigate("edit-profile") })
            SettingsRow(stringResource(R.string.preferencias_de_estudio), onClick = { onNavigate("preferences") })
            SettingsRow(stringResource(R.string.notificaciones), onClick = { onNavigate("notifications") })
            SettingsRow(
                title = stringResource(R.string.language),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        } catch (_: Exception) {
                            Toast.makeText(context, AppText.get(R.string.language_unavailable), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, AppText.get(R.string.language_unavailable), Toast.LENGTH_LONG).show()
                    }
                }
            )
            SettingsRow(
                title = stringResource(R.string.eliminar_mi_cuenta),
                onClick = { dialog = SettingsDialog.DeleteAccount },
                showDivider = false,
                color = DangerRed
            )
        }

        SettingsSectionTitle(stringResource(R.string.soporte))
        SettingsGroup {
            SettingsRow(stringResource(R.string.centro_de_ayuda), onClick = { dialog = SettingsDialog.Help })
            SettingsRow(
                title = stringResource(R.string.sugerencias),
                showDivider = false,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$SUPPORT_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, AppText.get(R.string.feedback_subject))
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            AppText.get(R.string.no_mail_app),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        SettingsSpacer(4)

        OutlinedButton(
            onClick = { dialog = SettingsDialog.Logout },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, LumiColors.Dark),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = LumiColors.White,
                contentColor = LumiColors.Dark
            )
        ) {
            Text(text = stringResource(R.string.cerrar_sesion), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        SettingsSpacer(2)

        Column(modifier = Modifier.padding(start = 4.dp, bottom = 24.dp)) {
            LegalLink(stringResource(R.string.terminos_y_condiciones)) { dialog = SettingsDialog.Terms }
            LegalLink(stringResource(R.string.politica_de_privacidad)) { dialog = SettingsDialog.Privacy }
            LegalLink("AGRADECIMIENTOS") { dialog = SettingsDialog.Credits }
        }
    }

    when (dialog) {
        SettingsDialog.Help -> InfoDialog(stringResource(R.string.centro_de_ayuda), stringResource(R.string.help_text)) { dialog = null }
        SettingsDialog.Terms -> InfoDialog(stringResource(R.string.terminos_y_condiciones_2), stringResource(R.string.terms_text)) { dialog = null }
        SettingsDialog.Privacy -> InfoDialog(stringResource(R.string.politica_de_privacidad_2), stringResource(R.string.privacy_text)) { dialog = null }
        SettingsDialog.Credits -> InfoDialog(stringResource(R.string.agradecimientos), stringResource(R.string.credits_text)) { dialog = null }
        SettingsDialog.Logout -> ConfirmDialog(
            title = stringResource(R.string.cerrar_sesion_2),
            text = stringResource(R.string.tendras_que_iniciar_sesion_de),
            confirmText = stringResource(R.string.cerrar_sesion_3),
            onConfirm = {
                dialog = null
                onLogoutClick()
            },
            onDismiss = { dialog = null }
        )
        SettingsDialog.DeleteAccount -> ConfirmDialog(
            title = stringResource(R.string.eliminar_tu_cuenta),
            text = stringResource(R.string.delete_account_text),
            confirmText = stringResource(R.string.eliminar),
            confirmColor = DangerRed,
            onConfirm = {
                dialog = null
                viewModel.deleteAccount { onLogoutClick() }
            },
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

@Composable
private fun LegalLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = LumiColors.YellowStrong
    )
}

