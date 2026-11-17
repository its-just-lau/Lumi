package com.lumi.app.features.subjects.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.lumi.app.core.components.LumiCard
import com.lumi.app.features.subjects.model.Subject
import com.lumi.app.ui.theme.LumiColors

@Composable
fun SubjectCard(
    subject: Subject,
    semesterName: String? = null,
    isDraft: Boolean = false,
    onClick: () -> Unit = {}
) {
    LumiCard(
        title = subject.name,
        modifier = Modifier.clickable {
            onClick()
        },
        containerColor = LumiColors.White
    ) {
        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(subject.color))
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when {
                isDraft -> "Borrador"
                subject.semesterId == null -> "Aprendizaje personal"
                else -> semesterName ?: "Materia"
            },
            fontSize = 11.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!isDraft) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progreso",
                    fontSize = 11.sp,
                    color = LumiColors.Dark
                )

                Text(
                    text = "${subject.progress}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = {
                    subject.progress / 100f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = LumiColors.YellowStrong,
                trackColor = LumiColors.WarmGray
            )
        } else {
            Text(
                text = "Completa su configuración",
                fontSize = 11.sp,
                color = LumiColors.Dark
            )
        }
    }
}
