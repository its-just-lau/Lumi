package com.lumi.app.mascot

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.R
import com.lumi.app.core.network.ErrorMessages
import com.lumi.app.ui.theme.LumiColors

/** Animaciones de Lumi (res/raw) según el estado de la pantalla. */
object LumiAnimations {
    val LOADING: Int get() = R.raw.lumi_cargando
    val EMPTY: Int get() = R.raw.lumi_estado_vacio
    val ALL_DONE: Int get() = R.raw.lumi_todo_listo
    val OFFLINE: Int get() = R.raw.lumi_sin_conexion
    val SERVER_ERROR: Int get() = R.raw.lumi_error_servidor
    val STREAK_ACTIVE: Int get() = R.raw.lumi_racha_activa
    val STREAK_NONE: Int get() = R.raw.lumi_sin_racha
    val REST: Int get() = R.raw.lumi_reposo
    val GLANCE: Int get() = R.raw.lumi_de_reojo
    val TASK_DONE: Int get() = R.raw.lumi_tarea_completada
    val STUDYING: Int get() = R.raw.lumi_estudiando
    val BREAK: Int get() = R.raw.lumi_descanso
    val SESSION_DONE: Int get() = R.raw.lumi_sesion_terminada
    val QUIZ_DONE: Int get() = R.raw.lumi_cuestionario_terminado
    val CARD_MISS: Int get() = R.raw.lumi_tarjeta_fallo
    val CARD_HIT: Int get() = R.raw.lumi_tarjeta_acierto
    val WEEKLY_GOAL: Int get() = R.raw.lumi_meta_semanal
    val OVERDUE: Int get() = R.raw.lumi_tarea_vencida
    val STORE: Int get() = R.raw.lumi_tienda
    val PURCHASE: Int get() = R.raw.lumi_compra_exitosa
    val NO_GEMS: Int get() = R.raw.lumi_sin_gemas
    val ACHIEVEMENT: Int get() = R.raw.lumi_logro
    val SLEEPING: Int get() = R.raw.lumi_dormida

    /** Sin conexión para fallos de red; error del servidor para el resto. */
    fun forError(message: String): Int = when (message) {
        ErrorMessages.NO_CONNECTION,
        ErrorMessages.NETWORK_PROBLEM,
        ErrorMessages.TIMEOUT -> OFFLINE
        else -> SERVER_ERROR
    }
}

/** Mascota animada con título y mensaje opcionales; [content] va debajo (botones). */
@Composable
fun MascotState(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    animationHeight: Dp = 200.dp,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MascotAnimation(
            resId = animationRes,
            modifier = Modifier
                .height(animationHeight)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(24.dp))
        )

        if (title != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark,
                textAlign = TextAlign.Center
            )
        }

        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = LumiColors.Dark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        content()
    }
}

/** Mascota con altura fija y proporción 9:16; con [centered] se centra en el ancho disponible. */
@Composable
fun MascotImage(
    @RawRes res: Int,
    height: Dp,
    modifier: Modifier = Modifier,
    centered: Boolean = false
) {
    val image: @Composable () -> Unit = {
        MascotAnimation(
            resId = res,
            modifier = Modifier
                .height(height)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(14.dp))
        )
    }

    if (centered) {
        androidx.compose.foundation.layout.Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { image() }
    } else {
        androidx.compose.foundation.layout.Box(modifier = modifier) { image() }
    }
}

/** Diálogo breve de celebración con la mascota; se cierra solo tras [autoDismissMillis]. */
@Composable
fun MascotCelebration(
    @RawRes animationRes: Int,
    title: String,
    onDismiss: () -> Unit,
    autoDismissMillis: Long = 3000L
) {
    androidx.compose.runtime.LaunchedEffect(animationRes) {
        kotlinx.coroutines.delay(autoDismissMillis)
        onDismiss()
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(28.dp),
            color = LumiColors.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotAnimation(
                    resId = animationRes,
                    modifier = Modifier
                        .height(260.dp)
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(18.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
