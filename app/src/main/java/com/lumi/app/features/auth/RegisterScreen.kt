package com.lumi.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    serverErrorMessage: String? = null
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isFormValid =
        name.isNotBlank() &&
                email.isNotBlank() &&
                email.contains("@") &&
                password.length >= 8 &&
                password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumiColors.Cream)
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.unete_a_lumi),
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Dark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.crea_tu_cuenta_y_comienza),
            style = MaterialTheme.typography.bodyLarge,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.nombre_completo)) },
            placeholder = { Text(stringResource(R.string.tu_nombre)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.correo_electronico)) },
            placeholder = { Text("ejemplo@correo.com") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.confirmar_contrasena)) },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        confirmPasswordVisible = !confirmPasswordVisible
                    }
                ) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (confirmPasswordVisible) {
                            AppText.get(R.string.ocultar_contrasena)
                        } else {
                            AppText.get(R.string.mostrar_contrasena)
                        }
                    )
                }
            },
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val shownError = errorMessage.ifBlank { serverErrorMessage.orEmpty() }

        if (shownError.isNotBlank()) {
            Text(
                text = shownError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = stringResource(R.string.la_contrasena_debe_tener_al),
            color = LumiColors.Dark,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        LumiButton(
            text = if (isLoading) stringResource(R.string.creando_cuenta) else stringResource(R.string.crear_cuenta),
            onClick = {
                when {
                    !email.contains("@") -> {
                        errorMessage = AppText.get(R.string.ingresa_un_correo_electronico_valido)
                    }
                    password.length < 8 -> {
                        errorMessage = AppText.get(R.string.la_contrasena_debe_tener_al)
                    }
                    password != confirmPassword -> {
                        errorMessage = AppText.get(R.string.las_contrasenas_no_coinciden)
                    }
                    else -> {
                        onRegisterClick(
                            name.trim(),
                            email.trim(),
                            password
                        )
                    }
                }
            },
            enabled = isFormValid && !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onLoginClick) {
            Text(
                text = stringResource(R.string.ya_tienes_cuenta_inicia_sesion),
                color = LumiColors.Dark
            )
        }

        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.volver), color = LumiColors.Dark)
        }
    }
}
