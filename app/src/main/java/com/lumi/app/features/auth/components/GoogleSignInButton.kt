package com.lumi.app.features.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.lumi.app.BuildConfig
import com.lumi.app.ui.theme.LumiColors
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

/**
 * "Continuar con Google": abre el selector de cuentas de Google y entrega el ID token,
 * que el backend verifica en POST /auth/google para iniciar sesión o crear la cuenta.
 */
@Composable
fun GoogleSignInButton(
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

            if (clientId.isBlank()) {
                onError(AppText.get(R.string.el_inicio_de_sesion_con))
                return@OutlinedButton
            }

            scope.launch {
                try {
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
                        .build()

                    val credential = CredentialManager.create(context)
                        .getCredential(context, request)
                        .credential

                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        onIdToken(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                    } else {
                        onError(AppText.get(R.string.no_se_pudo_iniciar_sesion_3))
                    }
                } catch (exception: GetCredentialCancellationException) {
                    // El usuario cerró el selector de cuentas: no es un error.
                } catch (exception: GetCredentialException) {
                    onError(AppText.get(R.string.no_se_pudo_iniciar_sesion_3))
                } catch (exception: Exception) {
                    onError(AppText.get(R.string.no_se_pudo_iniciar_sesion_3))
                }
            }
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, LumiColors.Dark),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = LumiColors.Dark)
    ) {
        Text(
            text = stringResource(R.string.continuar_con_google),
            fontWeight = FontWeight.Bold
        )
    }
}
