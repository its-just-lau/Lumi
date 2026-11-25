package com.lumi.app.features.settings

import com.lumi.app.core.network.UserService
import com.lumi.app.testutil.TestStrings
import com.lumi.app.data.models.user.PreferencesResponse
import com.lumi.app.data.models.user.UpdateProfileRequest
import com.lumi.app.data.models.user.UserProfileResponse
import com.lumi.app.data.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private class FakeUserService : UserService {
        var profile = UserProfileResponse("1", "Ana", "ana@mail.com", "America/Mexico_City", 3)
        var etag: String? = "\"3\""
        var updateStatus = 200
        var failWith: IOException? = null
        var preferences = PreferencesResponse(listOf("exams"), 45)
        var deleteStatus = 202
        var lastIfMatch: String? = null
        var lastPreferences: PreferencesResponse? = null

        override suspend fun getProfile() = profile

        override suspend fun getProfileResponse(): Response<UserProfileResponse> {
            failWith?.let { throw it }
            val headers = Headers.Builder().apply { etag?.let { add("ETag", it) } }.build()
            return Response.success(profile, headers)
        }

        override suspend fun updateProfile(
            etag: String,
            body: UpdateProfileRequest
        ): Response<UserProfileResponse> {
            lastIfMatch = etag
            if (updateStatus != 200) {
                return Response.error(
                    updateStatus,
                    "{}".toResponseBody("application/json".toMediaType())
                )
            }
            profile = profile.copy(name = body.name ?: profile.name, version = 4)
            return Response.success(profile, Headers.headersOf("ETag", "\"4\""))
        }

        override suspend fun deleteAccount(): Response<Unit> {
            return if (deleteStatus in 200..299) {
                Response.success(Unit)
            } else {
                Response.error(deleteStatus, "{}".toResponseBody("application/json".toMediaType()))
            }
        }

        override suspend fun getPreferences() = preferences

        override suspend fun updatePreferences(body: PreferencesResponse): PreferencesResponse {
            lastPreferences = body
            return body
        }
    }

    private lateinit var service: FakeUserService
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        TestStrings.install()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        service = FakeUserService()
        viewModel = AccountViewModel(UserRepository(service))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargaElPerfilYSuEtag() {
        viewModel.loadProfile()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertEquals("Ana", state.name)
        assertEquals("ana@mail.com", state.email)
        assertEquals("\"3\"", state.etag)
    }

    @Test
    fun siNoHayCabeceraUsaLaVersionComoEtag() {
        service.etag = null

        viewModel.loadProfile()

        assertEquals("\"3\"", viewModel.state.value.etag)
    }

    @Test
    fun noGuardaSinNombre() {
        viewModel.loadProfile()

        viewModel.saveProfile("   ")

        assertEquals("Escribe tu nombre.", viewModel.state.value.message)
        assertNull(service.lastIfMatch)
    }

    @Test
    fun guardaConIfMatchYActualizaElEtag() {
        viewModel.loadProfile()

        viewModel.saveProfile("  Ana María ")

        val state = viewModel.state.value
        assertEquals("\"3\"", service.lastIfMatch)
        assertEquals("Ana María", state.name)
        assertEquals("\"4\"", state.etag)
        assertTrue(state.saved)
    }

    @Test
    fun unConflictoRecargaElPerfil() {
        viewModel.loadProfile()
        service.updateStatus = 409

        viewModel.saveProfile("Otro")

        val state = viewModel.state.value
        assertFalse(state.saved)
        assertTrue(state.message.orEmpty().contains("cambió en otro dispositivo"))
        assertFalse(state.saving)
    }

    @Test
    fun sinInternetMuestraUnMensajeDeConexion() {
        service.failWith = IOException("sin red")

        viewModel.loadProfile()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertTrue(state.message.orEmpty().startsWith("Sin conexión"))
    }

    @Test
    fun cargaLasPreferenciasYConservaLasMetas() {
        viewModel.loadPreferences()

        assertEquals(45, viewModel.state.value.dailyGoalMinutes)
        assertEquals(listOf("exams"), viewModel.state.value.goals)
    }

    @Test
    fun guardarPreferenciasEnviaLasMetasQueYaHabia() {
        viewModel.loadPreferences()

        viewModel.savePreferences(90)

        assertEquals(90, service.lastPreferences?.dailyGoalMinutes)
        assertEquals(listOf("exams"), service.lastPreferences?.selectedGoals)
        assertTrue(viewModel.state.value.saved)
    }

    @Test
    fun eliminarCuentaLlamaAlCallbackSoloSiTieneExito() {
        var done = 0

        viewModel.deleteAccount { done++ }
        assertEquals(1, done)

        service.deleteStatus = 500
        viewModel.deleteAccount { done++ }
        assertEquals(1, done)
    }

    @Test
    fun consumirElMensajeLoLimpia() {
        viewModel.saveProfile("")
        viewModel.consumeMessage()

        assertNull(viewModel.state.value.message)
    }
}
