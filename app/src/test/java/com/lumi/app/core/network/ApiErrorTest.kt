package com.lumi.app.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import com.lumi.app.testutil.TestStrings
import org.junit.Before
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiErrorTest {

    @Before
    fun setUp() {
        TestStrings.install()
    }

    private fun httpError(status: Int, body: String): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(status, responseBody))
    }

    @Test
    fun usaElCodigoDelContratoCuandoExiste() {
        val error = httpError(
            409,
            """{"code":"EMAIL_ALREADY_EXISTS","message":"x","requestId":"abc"}"""
        )

        assertEquals("Este correo ya está registrado.", error.toUserMessage())
    }

    @Test
    fun usaElEstadoHttpCuandoElCodigoEsDesconocido() {
        val error = httpError(500, """{"code":"OTRO_CODIGO"}""")

        assertEquals(ErrorMessages.SERVER_PROBLEM, error.toUserMessage())
    }

    @Test
    fun usaElEstadoHttpCuandoElCuerpoNoEsJson() {
        val error = httpError(401, "esto no es json")

        assertEquals(ErrorMessages.SESSION_EXPIRED, error.toUserMessage())
    }

    @Test
    fun traduceLosErroresDeRed() {
        assertEquals(ErrorMessages.TIMEOUT, SocketTimeoutException().toUserMessage())
        assertEquals(ErrorMessages.NO_CONNECTION, UnknownHostException().toUserMessage())
        assertEquals(ErrorMessages.NETWORK_PROBLEM, IOException().toUserMessage())
        assertEquals(ErrorMessages.UNEXPECTED, IllegalStateException().toUserMessage())
    }

    @Test
    fun losEndpointsDeAutenticacionSonPublicos() {
        assertTrue(AuthEndpoints.isPublic("/api/v1/auth/login"))
        assertTrue(AuthEndpoints.isPublic("/api/v1/auth/refresh"))
        assertTrue(AuthEndpoints.isPublic("/auth/password/forgot"))
        assertFalse(AuthEndpoints.isPublic("/api/v1/auth/me"))
        assertFalse(AuthEndpoints.isPublic("/api/v1/subjects"))
    }
}
