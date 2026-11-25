
package com.lumi.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.features.auth.components.GoogleSignInButton
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import com.lumi.app.R
import com.lumi.app.core.i18n.AppText

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onGoogleIdToken: (String) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumiColors.Cream)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.lumi),
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Dark
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.que_bueno_verte_de_nuevo),
            style = MaterialTheme.typography.headlineMedium,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.inicia_sesion_para_continuar_aprendiendo),
            style = MaterialTheme.typography.bodyMedium,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.correo_electronico)) },
            placeholder = { Text("ejemplo@correo.com") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.contrasena)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            AppText.get(R.string.ocultar_contrasena)
                        } else {
                            AppText.get(R.string.mostrar_contrasena)
                        }
                    )
                }
            },
            shape = RoundedCornerShape(14.dp)
        )


        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = stringResource(R.string.olvidaste_tu_contrasena),
                color = LumiColors.Dark
            )
        }

        val shownError = errorMessage ?: googleError

        if (!shownError.isNullOrBlank()) {
            Text(
                text = shownError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LumiButton(
            text = if (isLoading) stringResource(R.string.iniciando_sesion) else stringResource(R.string.iniciar_sesion),
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    onLoginClick(email.trim(), password)
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
        )

        Spacer(modifier = Modifier.height(10.dp))

        GoogleSignInButton(
            onIdToken = {
                googleError = null
                onGoogleIdToken(it)
            },
            onError = { googleError = it },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onRegisterClick) {
            Text(
                text = stringResource(R.string.no_tienes_cuenta_crear_una),
                color = LumiColors.Dark
            )
        }

        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.volver), color = LumiColors.Dark)
        }
    }
}
