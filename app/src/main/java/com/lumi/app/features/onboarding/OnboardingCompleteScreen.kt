package com.lumi.app.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.lumi.app.core.components.LumiButton
import com.lumi.app.data.local.OnboardingPreferencesRepository
import com.lumi.app.features.onboarding.components.MascotPlaceholder
import com.lumi.app.ui.theme.LumiColors
import kotlinx.coroutines.flow.first
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

@Composable
fun OnboardingCompleteScreen(
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current

    val repository = remember {
        OnboardingPreferencesRepository(context)
    }

    var dailyGoal by remember {
        mutableIntStateOf(15)
    }

    LaunchedEffect(Unit) {
        val preferences = repository.preferences.first()

        dailyGoal = preferences.dailyGoalMinutes

        repository.saveOnboardingCompleted(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumiColors.Cream)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.bienvenido_a_lumi),
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        MascotPlaceholder(
            message = stringResource(R.string.estoy_feliz_de_acompanarte),
            animationRes = R.raw.lumi_todo_listo,
            animationHeight = 240.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.tu_camino_de_aprendizaje_comienza),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.tu_meta_diaria_sera_de, dailyGoal),
            style = MaterialTheme.typography.bodyLarge,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        LumiButton(
            text = stringResource(R.string.ir_a_lumi),
            onClick = onContinueClick
        )
    }
}
