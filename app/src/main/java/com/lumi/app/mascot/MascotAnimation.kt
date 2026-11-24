package com.lumi.app.mascot

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RawRes
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.min

/**
 * Reproduce una animación (GIF o WebP animado) de la mascota guardada en res/raw.
 *
 * Se dibuja directamente en un Canvas de Compose, así que ocupa exactamente el
 * tamaño que indique el [modifier] y la imagen se ajusta completa, sin recortes.
 * En Android 9 o superior se anima en bucle; en versiones anteriores se muestra
 * el primer cuadro.
 */
@Composable
fun MascotAnimation(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    var frameTick by remember(resId) { mutableIntStateOf(0) }

    val drawable: Drawable? = remember(resId) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeDrawable(ImageDecoder.createSource(context.resources, resId))
            } else {
                BitmapDrawable(
                    context.resources,
                    BitmapFactory.decodeResource(context.resources, resId)
                )
            }
        }.getOrNull()
    }

    DisposableEffect(drawable) {
        val handler = Handler(Looper.getMainLooper())
        val animated = drawable as? AnimatedImageDrawable
        if (animated != null) {
            animated.callback = object : Drawable.Callback {
                override fun invalidateDrawable(who: Drawable) {
                    frameTick++
                }

                override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                    handler.postAtTime(what, `when`)
                }

                override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                    handler.removeCallbacks(what)
                }
            }
            animated.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
            animated.start()
        }
        onDispose {
            animated?.stop()
            animated?.callback = null
        }
    }

    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    Canvas(modifier = semanticsModifier) {
        frameTick // leerlo hace que se vuelva a dibujar en cada cuadro
        val d = drawable ?: return@Canvas
        val iw = d.intrinsicWidth.takeIf { it > 0 } ?: return@Canvas
        val ih = d.intrinsicHeight.takeIf { it > 0 } ?: return@Canvas

        // Ajuste completo y centrado, sin recortar
        val scale = min(size.width / iw, size.height / ih)
        val w = (iw * scale).toInt()
        val h = (ih * scale).toInt()
        val left = ((size.width - w) / 2f).toInt()
        val top = ((size.height - h) / 2f).toInt()

        d.setBounds(left, top, left + w, top + h)
        drawIntoCanvas { canvas -> d.draw(canvas.nativeCanvas) }
    }
}
