package com.lumi.app.data.repositories

import com.lumi.app.core.network.TaskService
import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.tasks.CompleteTaskRequest
import com.lumi.app.data.models.tasks.TaskCompletionResponse
import com.lumi.app.data.models.tasks.TaskInput
import com.lumi.app.data.models.tasks.TaskResponse
import com.lumi.app.core.storage.db.CacheStore
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class TaskRepository(
    private val taskService: TaskService,
    private val cache: CacheStore? = null
) {

    /** Todas las tareas: sigue nextCursor hasta que no haya más páginas. */
    suspend fun getAllTasks(): Result<List<TaskResponse>> {
        val fetch: suspend () -> Result<List<TaskResponse>> = { fetchAllTasks() }

        return cache?.cached("tasks", TaskResponse::class.java, { it.id }, fetch) ?: fetch()
    }

    private suspend fun fetchAllTasks(): Result<List<TaskResponse>> {
        return safeCall {
            val all = mutableListOf<TaskResponse>()
            var cursor: String? = null
            var pages = 0

            do {
                val page: PageResponse<TaskResponse> = taskService.getTasks(cursor = cursor)
                all.addAll(page.items)

                cursor = if (page.hasMore) page.nextCursor else null
                pages++
            } while (cursor != null && pages < MAX_PAGES)

            all
        }
    }

    suspend fun createTask(task: TaskInput): Result<TaskResponse> {
        return safeCall { taskService.createTask(task) }
    }

    suspend fun getTask(taskId: String): Result<Response<TaskResponse>> {
        return safeCall { taskService.getTask(taskId) }
    }

    suspend fun updateTask(
        taskId: String,
        etag: String,
        task: TaskInput
    ): Result<Response<TaskResponse>> {
        return safeCall { taskService.updateTask(taskId, etag, task) }
    }

    suspend fun deleteTask(taskId: String): Result<Response<Unit>> {
        return safeCall { taskService.deleteTask(taskId) }
    }

    /** La Idempotency-Key permite reintentar sin duplicar la recompensa. */
    suspend fun completeTask(
        taskId: String,
        idempotencyKey: String,
        completedAt: String,
        timeZone: String
    ): Result<TaskCompletionResponse> {
        return safeCall {
            taskService.completeTask(
                taskId = taskId,
                idempotencyKey = idempotencyKey,
                body = CompleteTaskRequest(completedAt, timeZone)
            )
        }
    }

    suspend fun reopenTask(
        taskId: String,
        idempotencyKey: String
    ): Result<TaskCompletionResponse> {
        return safeCall { taskService.reopenTask(taskId, idempotencyKey) }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private companion object {
        const val MAX_PAGES = 200
    }
}
