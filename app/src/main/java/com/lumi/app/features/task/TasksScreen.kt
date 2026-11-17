package com.lumi.app.features.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.core.components.LumiButton
import com.lumi.app.ui.theme.LumiColors

@Composable
fun TasksScreen(
    onNavigate: (String) -> Unit
) {

    val filters = listOf(
        "Todas",
        "Hoy",
        "Próximas",
        "Vencidas",
        "Completadas"
    )

    var selectedFilter by remember {
        mutableStateOf("Todas")
    }

    Scaffold(
        containerColor = LumiColors.Cream,

        bottomBar = {
            LumiBottomBar(
                selectedRoute = "tasks",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {

            // ENCABEZADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Mis tareas",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                LumiButton(
                    text = "+",
                    onClick = {
                        // Próximamente: crear tarea
                    },
                    modifier = Modifier.width(55.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FILTROS
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(filters) { filter ->

                    FilterChip(
                        selected = selectedFilter == filter,

                        onClick = {
                            selectedFilter = filter
                        },

                        label = {
                            Text(filter)
                        },

                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LumiColors.Yellow,
                            selectedLabelColor = LumiColors.Dark,
                            containerColor = LumiColors.White,
                            labelColor = LumiColors.Dark
                        ),

                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = LumiColors.WarmGray,
                            selectedBorderColor = LumiColors.YellowStrong
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CONTENIDO
            Text(
                text = when (selectedFilter) {
                    "Hoy" -> "Para hoy"
                    "Próximas" -> "Próximas tareas"
                    "Vencidas" -> "Tareas vencidas"
                    "Completadas" -> "Tareas completadas"
                    else -> "Todas tus tareas"
                },
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ESTADO VACÍO TEMPORAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LumiColors.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "🦎",
                        fontSize = 55.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¡Todo listo para organizarte!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Aquí aparecerán tus tareas. " +
                                "Agrega una nueva para comenzar.",
                        fontSize = 14.sp,
                        color = LumiColors.Dark.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
