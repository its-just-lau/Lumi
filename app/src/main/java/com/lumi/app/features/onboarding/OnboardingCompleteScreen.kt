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
            text = "¡Bienvenido a Lumi!",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        MascotPlaceholder(
            modifier = Modifier.height(190.dp),
            message = "¡Estoy feliz de acompañarte!"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tu camino de aprendizaje comienza hoy.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tu meta diaria será de $dailyGoal minutos. Puedes modificarla cuando quieras.",
            style = MaterialTheme.typography.bodyLarge,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        LumiButton(
            text = "Ir a Lumi",
            onClick = onContinueClick
        )
    }
}
