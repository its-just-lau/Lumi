package com.lumi.app.features.calendar

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

private const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.events"

/** Recuerda si el usuario ya conectó su Google Calendar en este dispositivo. */
class GoogleCalendarPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("lumi_google_calendar", Context.MODE_PRIVATE)

    var connected: Boolean
        get() = prefs.getBoolean("connected", false)
        set(value) = prefs.edit().putBoolean("connected", value).apply()
}

/**
 * Pide a Google un token de acceso para Calendar.
 *
 * request(interactive, onToken):
 *  - Si el usuario ya dio permiso, entrega el token sin mostrar nada.
 *  - Si falta el permiso y interactive = true, muestra la pantalla de Google para aceptarlo.
 *  - Si falta el permiso e interactive = false, no hace nada.
 */
class GoogleCalendarAccess(
    val request: (interactive: Boolean, onToken: (String) -> Unit) -> Unit
)

@Composable
fun rememberGoogleCalendarAccess(
    onError: (String) -> Unit
): GoogleCalendarAccess {
    val context = LocalContext.current

    var pending by remember { mutableStateOf<((String) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val callback = pending
        pending = null

        try {
            val authorization = Identity.getAuthorizationClient(context)
                .getAuthorizationResultFromIntent(result.data)
            val token = authorization.accessToken

            if (token != null) {
                callback?.invoke(token)
            } else {
                onError(AppText.get(R.string.no_se_pudo_conectar_con))
            }
        } catch (exception: Exception) {
            // El usuario canceló o Google rechazó el permiso
            onError(AppText.get(R.string.no_se_pudo_conectar_con))
        }
    }

    return GoogleCalendarAccess { interactive, onToken ->
        val authRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(CALENDAR_SCOPE)))
            .build()

        Identity.getAuthorizationClient(context)
            .authorize(authRequest)
            .addOnSuccessListener { result ->
                val pendingIntent = result.pendingIntent

                if (result.hasResolution() && pendingIntent != null) {
                    if (interactive) {
                        pending = onToken
                        launcher.launch(
                            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                        )
                    }
                } else {
                    val token = result.accessToken

                    if (token != null) {
                        onToken(token)
                    } else if (interactive) {
                        onError(AppText.get(R.string.no_se_pudo_conectar_con))
                    }
                }
            }
            .addOnFailureListener {
                if (interactive) {
                    onError(AppText.get(R.string.no_se_pudo_conectar_con))
                }
            }
    }
}
