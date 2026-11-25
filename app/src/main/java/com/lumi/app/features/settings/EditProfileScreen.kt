package com.lumi.app.features.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.core.components.LumiTextField
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: AccountViewModel = viewModel(
        factory = AccountViewModelFactory(LumiApp.container.userRepository)
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onBack()
        }
    }

    var name by remember(state.name) { mutableStateOf(state.name) }

    SettingsScaffold(title = stringResource(R.string.editar_perfil), onBack = onBack) {
        Spacer(modifier = Modifier.height(4.dp))

        LumiTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.nombre),
            enabled = !state.loading && !state.saving
        )

        LumiTextField(
            value = state.email,
            onValueChange = {},
            label = stringResource(R.string.correo),
            enabled = false
        )

        LumiTextField(
            value = state.timeZone,
            onValueChange = {},
            label = stringResource(R.string.zona_horaria),
            enabled = false
        )

        Text(
            text = stringResource(R.string.el_correo_y_la_zona),
            fontSize = 12.sp,
            color = LumiColors.Dark.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        LumiButton(
            text = if (state.saving) "Guardando..." else stringResource(R.string.guardar_cambios),
            onClick = { viewModel.saveProfile(name) },
            enabled = !state.loading && !state.saving
        )
    }
}
