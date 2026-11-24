package com.lumi.app.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

/** Cuerpo de error del contrato (ApiError): code, message, details, requestId. */
data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
    val details: Map<String, Any?>? = null,
    val requestId: String? = null
)

private val errorGson = Gson()

/** Lee el cuerpo de error del API. Solo se puede leer una vez por excepción. */
fun HttpException.toApiErrorBody(): ApiErrorBody? {
    return try {
        response()?.errorBody()?.string()?.let { json ->
            errorGson.fromJson(json, ApiErrorBody::class.java)
        }
    } catch (exception: Exception) {
        null
    }
}

/** Convierte cualquier error de red o del API en un mensaje en español para el usuario. */
fun Throwable.toUserMessage(
    default: String = ErrorMessages.UNEXPECTED
): String {
    return when (this) {
        is HttpException -> {
            val body = toApiErrorBody()
            ErrorMessages.forCode(body?.code) ?: ErrorMessages.forStatus(code())
        }
        is SocketTimeoutException -> ErrorMessages.TIMEOUT
        is UnknownHostException, is ConnectException -> ErrorMessages.NO_CONNECTION
        is IOException -> ErrorMessages.NETWORK_PROBLEM
        else -> default
    }
}

/** Mensajes para el usuario según los códigos del catálogo del contrato (§2.9). */
object ErrorMessages {

    val UNEXPECTED: String get() = AppText.get(R.string.error_unexpected)
    val TIMEOUT: String get() = AppText.get(R.string.error_timeout)
    val NO_CONNECTION: String get() = AppText.get(R.string.error_no_connection)
    val NETWORK_PROBLEM: String get() = AppText.get(R.string.error_network_problem)
    val SESSION_EXPIRED: String get() = AppText.get(R.string.error_session_expired)
    val VALIDATION: String get() = AppText.get(R.string.error_validation)
    val SERVER_PROBLEM: String get() = AppText.get(R.string.error_server_problem)

    fun forCode(code: String?): String? {
        return when (code) {
            "VALIDATION_ERROR" -> VALIDATION
            "EMAIL_ALREADY_EXISTS" -> AppText.get(R.string.este_correo_ya_esta_registrado)
            "INVALID_CREDENTIALS" -> AppText.get(R.string.correo_o_contrasena_incorrectos)
            "TOKEN_EXPIRED", "REFRESH_TOKEN_INVALID" -> SESSION_EXPIRED
            "RESOURCE_NOT_FOUND" -> AppText.get(R.string.no_se_encontro_el_elemento)
            "RESOURCE_DELETED" -> AppText.get(R.string.este_elemento_ya_fue_eliminado)
            "IDEMPOTENCY_CONFLICT" -> AppText.get(R.string.esta_operacion_ya_se_habia)
            "VERSION_CONFLICT" -> AppText.get(R.string.este_elemento_cambio_en_otro)
            "SYNC_TOKEN_EXPIRED", "SYNC_RUN_EXPIRED" -> AppText.get(R.string.necesitamos_volver_a_sincronizar_tus)
            "INSUFFICIENT_GEMS" -> AppText.get(R.string.no_tienes_gemas_suficientes)
            "INSUFFICIENT_ITEMS" -> AppText.get(R.string.no_tienes_suficientes_unidades_de)
            "ITEM_NOT_ELIGIBLE" -> AppText.get(R.string.no_se_puede_usar_este)
            "FILE_TOO_LARGE" -> AppText.get(R.string.el_archivo_es_demasiado_grande)
            "UNSUPPORTED_FILE_TYPE" -> AppText.get(R.string.este_tipo_de_archivo_no)
            "PROCESSING_FAILED" -> AppText.get(R.string.no_se_pudo_procesar_el)
            "QUOTA_EXCEEDED" -> AppText.get(R.string.alcanzaste_el_limite_de_uso)
            "RATE_LIMITED" -> AppText.get(R.string.demasiados_intentos_espera_un_momento)
            else -> null
        }
    }

    fun forStatus(status: Int): String {
        return when (status) {
            401 -> SESSION_EXPIRED
            403 -> AppText.get(R.string.no_tienes_permiso_para_hacer)
            404 -> AppText.get(R.string.no_se_encontro_el_elemento)
            409 -> AppText.get(R.string.hubo_un_conflicto_con_los)
            422 -> VALIDATION
            429 -> AppText.get(R.string.demasiados_intentos_espera_un_momento)
            in 500..599 -> SERVER_PROBLEM
            else -> UNEXPECTED
        }
    }
}
