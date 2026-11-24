package com.lumi.app.features.subjects.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.mascot.LumiAnimations
import com.lumi.app.mascot.MascotAnimation
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@Composable
fun EmptySubjectsState(
    onAddSubject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 35.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MascotAnimation(
            resId = LumiAnimations.EMPTY,
            modifier = Modifier
                .height(190.dp)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(24.dp))
        )

        Text(
            text = stringResource(R.string.aun_no_hay_materias_aqui),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LumiColors.Dark
        )

        Text(
            text = stringResource(R.string.agrega_una_materia_para_comenzar),
            fontSize = 14.sp,
            color = LumiColors.Dark
        )

        LumiButton(
            text = stringResource(R.string.agregar_materia),
            onClick = onAddSubject
        )
    }
}
