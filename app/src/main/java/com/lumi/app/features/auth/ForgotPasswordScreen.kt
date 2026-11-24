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
import com.lumi.app.core.components.LumiButton
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

@Composable
fun ForgotPasswordScreen(
    onSendClick: (String) -> Unit,
    onHaveCodeClick: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null
) {
    var email by remember { mutableStateOf("") }

    val isEmailValid = email.isNotBlank() && email.contains("@")

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
            text = stringResource(R.string.lumi),
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LumiColors.Dark
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.recupera_tu_contrasena),
            style = MaterialTheme.typography.headlineMedium,
            color = LumiColors.Dark,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.escribe_tu_correo_y_te),
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

        Spacer(modifier = Modifier.height(12.dp))

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        if (!successMessage.isNullOrBlank()) {
            Text(
                text = successMessage,
                color = LumiColors.Dark,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LumiButton(
            text = if (isLoading) "Enviando..." else stringResource(R.string.enviar_instrucciones),
            onClick = { onSendClick(email.trim()) },
            enabled = isEmailValid && !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onHaveCodeClick) {
            Text(
                text = stringResource(R.string.ya_tengo_mi_codigo),
                color = LumiColors.Dark
            )
        }

        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.volver), color = LumiColors.Dark)
        }
    }
}
