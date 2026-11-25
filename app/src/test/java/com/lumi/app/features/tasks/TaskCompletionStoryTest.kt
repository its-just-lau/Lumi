package com.lumi.app.features.tasks

import com.lumi.app.core.network.ErrorMessages
import com.lumi.app.core.network.SubjectService
import com.lumi.app.core.network.TaskService
import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.data.models.tasks.CompleteTaskRequest
import com.lumi.app.data.models.tasks.RewardResult
import com.lumi.app.data.models.tasks.TaskCompletionResponse
import com.lumi.app.data.models.tasks.TaskInput
import com.lumi.app.data.models.tasks.TaskResponse
import com.lumi.app.data.models.tasks.WalletResponse
import com.lumi.app.data.repositories.SubjectRepository
import com.lumi.app.data.repositories.TaskRepository
import com.lumi.app.features.subjects.model.SemesterInput
import com.lumi.app.features.subjects.model.SubjectInput
import com.lumi.app.testutil.TestStrings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneId

/**
 * HU-001 "Completar una tarea y recibir recompensa".
 * Cada prueba lleva el código del criterio de aceptación (CA) del documento de trazabilidad.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskCompletionStoryTest {

    // ---------- Dobles de prueba ----------

    private class FakeTaskService(var tasks: List<TaskResponse>) : TaskService {
        var completeCalls = 0
        var reopenCalls = 0
        var lastIdempotencyKey: String? = null
        var lastBody: CompleteTaskRequest? = null
        var failWith: Exception? = null
        var gate: CompletableDeferred<Unit>? = null
        var reward: RewardResult? = RewardResult(
            status = "confirmed",
            xpAwarded = 20,
            gemsAwarded = 5,
            wallet = WalletResponse(xp = 120, gems = 35)
        )

        override suspend fun getTasks(
            syncToken: String?,
            syncRun: String?,
            limit: Int,
            cursor: String?
        ) = PageResponse(
            items = tasks,
            nextCursor = null,
            hasMore = false,
            syncToken = "t",
            serverTime = "2026-11-24T00:00:00Z"
        )

        override suspend fun completeTask(
            taskId: String,
            idempotencyKey: String,
            body: CompleteTaskRequest
        ): TaskCompletionResponse {
            completeCalls++
            lastIdempotencyKey = idempotencyKey
            lastBody = body
            gate?.await()
            failWith?.let { throw it }
            val done = tasks.first { it.id == taskId }
                .copy(status = "completed", completedAt = body.completedAt)
            return TaskCompletionResponse(task = done, rewardResult = reward)
        }

        override suspend fun reopenTask(
            taskId: String,
            idempotencyKey: String
        ): TaskCompletionResponse {
            reopenCalls++
            lastIdempotencyKey = idempotencyKey
            val open = tasks.first { it.id == taskId }.copy(status = "pending", completedAt = null)
            return TaskCompletionResponse(task = open, rewardResult = null)
        }

        override suspend fun createTask(task: TaskInput): TaskResponse = throw NotImplementedError()
        override suspend fun getTask(taskId: String): Response<TaskResponse> = throw NotImplementedError()
        override suspend fun updateTask(
            taskId: String,
            etag: String,
            task: TaskInput
        ): Response<TaskResponse> = throw NotImplementedError()

        override suspend fun deleteTask(taskId: String): Response<Unit> = throw NotImplementedError()
    }

    private class FakeSubjectService : SubjectService {
        override suspend fun getSubjects(
            syncToken: String?,
            syncRun: String?,
            limit: Int,
            cursor: String?
        ) = PageResponse<SubjectResponse>(
            items = emptyList(),
            hasMore = false,
            syncToken = "t",
            serverTime = "2026-11-24T00:00:00Z"
        )

        override suspend fun getSemesters(
            syncToken: String?,
            syncRun: String?,
            limit: Int,
            cursor: String?
        ): PageResponse<SemesterResponse> = throw NotImplementedError()

        override suspend fun createSemester(semester: SemesterInput): SemesterResponse = throw NotImplementedError()
        override suspend fun getSemester(semesterId: String): Response<SemesterResponse> = throw NotImplementedError()
        override suspend fun updateSemester(
            semesterId: String,
            etag: String,
            semester: SemesterInput
        ): Response<SemesterResponse> = throw NotImplementedError()

        override suspend fun deleteSemester(semesterId: String): Response<Unit> = throw NotImplementedError()
        override suspend fun createSubject(subject: SubjectInput): SubjectResponse = throw NotImplementedError()
        override suspend fun getSubject(subjectId: String): Response<SubjectResponse> = throw NotImplementedError()
        override suspend fun updateSubject(
            subjectId: String,
            etag: String,
            subject: SubjectInput
        ): Response<SubjectResponse> = throw NotImplementedError()

        override suspend fun deleteSubject(subjectId: String): Response<Unit> = throw NotImplementedError()
    }

    private val pending = TaskResponse(id = "t1", title = "Entregar práctica", status = "pending")
    private lateinit var service: FakeTaskService
    private lateinit var viewModel: TaskViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        TestStrings.install()
        service = FakeTaskService(listOf(pending))
        viewModel = TaskViewModel(
            TaskRepository(service),
            SubjectRepository(FakeSubjectService())
        )
        viewModel.loadTasks()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun taskInList(id: String): TaskResponse =
        (viewModel.uiState.value as TasksUiState.Success).tasks.first { it.id == id }

    // ---------- Criterios de aceptación ----------

    /** CA-01: tarea pendiente + completar => queda completada y se informa la recompensa. */
    @Test
    fun ca01_completarTareaPendienteMarcaCompletadaYMuestraRecompensa() {
        viewModel.toggleComplete(pending)

        assertEquals("completed", taskInList("t1").status)
        assertEquals("¡Tarea completada! +20 XP · +5 gemas", viewModel.message.value)
    }

    /** CA-02: la API recibe la hora de cierre en ISO-8601, la zona horaria y una llave de idempotencia. */
    @Test
    fun ca02_laPeticionLlevaFechaZonaHorariaYLlaveDeIdempotencia() {
        viewModel.toggleComplete(pending)

        assertEquals(1, service.completeCalls)
        assertTrue(!service.lastIdempotencyKey.isNullOrBlank())
        assertEquals(ZoneId.systemDefault().id, service.lastBody?.timeZone)
        assertNotNull(Instant.parse(service.lastBody!!.completedAt))
    }

    /** CA-03: si la recompensa ya se había otorgado, se completa sin prometer XP ni gemas. */
    @Test
    fun ca03_recompensaYaOtorgadaSoloMuestraTareaCompletada() {
        service.reward = RewardResult(
            status = "not_granted",
            reason = "ALREADY_REWARDED",
            xpAwarded = 0,
            gemsAwarded = 0
        )

        viewModel.toggleComplete(pending)

        assertEquals("completed", taskInList("t1").status)
        assertEquals("¡Tarea completada!", viewModel.message.value)
    }

    /** CA-04: sin conexión se avisa al usuario y la tarea sigue pendiente. */
    @Test
    fun ca04_sinConexionMuestraErrorYNoCambiaLaTarea() {
        service.failWith = UnknownHostException("sin red")

        viewModel.toggleComplete(pending)

        assertEquals("pending", taskInList("t1").status)
        assertEquals(ErrorMessages.NO_CONNECTION, viewModel.message.value)
    }

    /** CA-05: un segundo toque mientras la operación sigue en curso no envía otra petición. */
    @Test
    fun ca05_dobleToqueEnviaUnaSolaPeticion() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        service.gate = gate

        viewModel.toggleComplete(pending)
        viewModel.toggleComplete(pending)
        gate.complete(Unit)

        assertEquals(1, service.completeCalls)
        assertEquals("completed", taskInList("t1").status)
    }

    /** CA-06: una tarea completada se puede reabrir y vuelve a pendiente. */
    @Test
    fun ca06_reabrirTareaCompletadaLaDejaPendiente() {
        val completed = pending.copy(status = "completed")
        service.tasks = listOf(completed)
        viewModel.loadTasks()

        viewModel.toggleComplete(completed)

        assertEquals(1, service.reopenCalls)
        assertEquals("pending", taskInList("t1").status)
        assertEquals("Tarea reabierta", viewModel.message.value)
    }

    /** CA-07: el repositorio entrega la llave de idempotencia al servicio y envuelve los errores. */
    @Test
    fun ca07_repositorioEnviaLaLlaveYDevuelveFalloSiLaRedFalla() = runBlocking {
        val repository = TaskRepository(service)

        val ok = repository.completeTask("t1", "llave-123", "2026-11-24T10:00:00Z", "America/Mexico_City")
        assertTrue(ok.isSuccess)
        assertEquals("llave-123", service.lastIdempotencyKey)

        service.failWith = UnknownHostException("sin red")
        val failed = repository.completeTask("t1", "llave-456", "2026-11-24T10:01:00Z", "America/Mexico_City")
        assertTrue(failed.isFailure)
    }
}
