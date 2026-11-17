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
        Text(
            text = "Aún no hay materias aquí",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LumiColors.Dark
        )

        Text(
            text = "Agrega una materia para comenzar a organizar tu aprendizaje.",
            fontSize = 14.sp,
            color = LumiColors.Dark
        )

        LumiButton(
            text = "Agregar materia",
            onClick = onAddSubject
        )
    }
}
