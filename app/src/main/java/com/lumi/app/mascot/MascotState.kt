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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
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

/** Diálogo de la racha: Lumi celebra si hay racha y se anima si aún no. */
@Composable
fun StreakDialog(streakDays: Int, onDismiss: () -> Unit) {
    val active = streakDays > 0
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LumiColors.White,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.entendido)) }
        },
        text = {
            MascotState(
                animationRes = if (active) LumiAnimations.STREAK_ACTIVE else LumiAnimations.STREAK_NONE,
                title = stringResource(
                    if (active) R.string.streak_active_title else R.string.streak_none_title
                ),
                message = if (active) {
                    stringResource(R.string.streak_active_message, streakDays)
                } else {
                    stringResource(R.string.streak_none_message)
                },
                animationHeight = 230.dp
            )
        }
    )
}
