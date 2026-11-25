package com.lumi.app.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.lumi.app.ui.theme.LumiColors

/** Bloque gris con un brillo que se desplaza: marca el lugar de un contenido que aún carga. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    val transition = rememberInfiniteTransition(label = "skeleton")

    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton-shift"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            LumiColors.WarmGray,
            LumiColors.Cream,
            LumiColors.WarmGray
        ),
        start = Offset(shift - 700f, 0f),
        end = Offset(shift, 0f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/** Fila de lista (icono + dos líneas), con el tamaño de las tarjetas de tareas y lecciones. */
@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(11.dp)
                )
            }
        }
    }
}

/** Varias filas de lista para cargas de pantalla completa. */
@Composable
fun SkeletonList(
    count: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) { SkeletonListItem() }
    }
}

/** Cuadrícula de dos columnas, con el aspecto de las tarjetas de materias. */
@Composable
fun SkeletonGrid(
    rows: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(132.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SkeletonBox(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            SkeletonBox(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(14.dp)
                            )
                            SkeletonBox(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
