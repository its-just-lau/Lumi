
package com.lumi.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.mascot.MascotAnimation
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumiColors.Cream)
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.lumi),
            fontSize = 54.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Dark
        )

        Spacer(modifier = Modifier.height(10.dp))

        MascotAnimation(
            resId = R.raw.lumi_saludo,
            modifier = Modifier
                .height(minOf(445.dp, (LocalConfiguration.current.screenHeightDp * 0.52f).dp))
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(28.dp))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.aprender_puede_ser_increible),
            style = MaterialTheme.typography.headlineMedium,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.organiza_tus_materias_alcanza_tus),
            style = MaterialTheme.typography.bodyLarge,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        LumiButton(
            text = stringResource(R.string.iniciar_sesion),
            onClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        LumiButton(
            text = stringResource(R.string.crear_cuenta),
            onClick = onRegisterClick,
            backgroundColor = LumiColors.WarmGray
        )
    }
}
