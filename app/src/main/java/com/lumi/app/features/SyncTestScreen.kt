package com.lumi.app.features

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumi.app.core.network.RetrofitClient
import com.lumi.app.core.network.SyncService
import kotlinx.coroutines.launch

@Composable
fun SyncTestScreen() {
    var result by remember { mutableStateOf("Presiona el botón para probar la conexión.") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Prueba de conexión con Lumi",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    result = "Conectando con Prism..."

                    try {
                        val service = RetrofitClient.retrofit
                            .create(SyncService::class.java)

                        val response = service.getSyncState(
                            authorization = "Bearer lumi-mock-token"
                        )

                        result = """
                            ¡Conexión exitosa!

                            Hora del servidor: ${response.serverTime}
                            Token de sincronización: ${response.syncRunToken}
                            Vencimiento: ${response.expiresAt}

                            Colecciones:
                            ${response.collections.joinToString("\n") {
                            "${it.name} - Orden ${it.order}"
                        }}
                        """.trimIndent()

                    } catch (e: Exception) {
                        result = "Error de conexión: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading
        ) {
            Text(if (loading) "Conectando..." else "Probar conexión")
        }

        Text(
            text = result,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
