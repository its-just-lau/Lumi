package com.lumi.app.features.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

private val GOAL_OPTIONS = listOf(15, 30, 45, 60, 90, 120, 180)

@Composable
fun PreferencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: AccountViewModel = viewModel(
        factory = AccountViewModelFactory(LumiApp.container.userRepository)
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPreferences() }

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

    var selected by remember(state.dailyGoalMinutes) { mutableIntStateOf(state.dailyGoalMinutes) }
    // Si el servidor devolvió un valor distinto a las opciones, también se muestra.
    val options = (GOAL_OPTIONS + state.dailyGoalMinutes).distinct().sorted()

    SettingsScaffold(title = stringResource(R.string.preferencias_de_estudio), onBack = onBack) {
        SettingsSectionTitle(stringResource(R.string.meta_diaria_de_estudio))

        Text(
            text = stringResource(R.string.cuantos_minutos_quieres_estudiar_cada),
            modifier = Modifier.padding(horizontal = 4.dp),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = LumiColors.Dark.copy(alpha = 0.7f)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(options) { minutes ->
                val isSelected = minutes == selected
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) LumiColors.Yellow else LumiColors.White,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) LumiColors.Dark else LumiColors.WarmGray
                    ),
                    modifier = Modifier.clickable { selected = minutes }
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = if (minutes >= 60 && minutes % 60 == 0) stringResource(R.string.h, minutes / 60) else stringResource(R.string.min, minutes),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LumiButton(
            text = if (state.saving) "Guardando..." else stringResource(R.string.guardar),
            onClick = { viewModel.savePreferences(selected) },
            enabled = !state.loading && !state.saving
        )
    }
}
