package com.lumi.app.core.network

import com.lumi.app.data.models.subjects.PageResponse
import com.lumi.app.data.models.tasks.CompleteTaskRequest
import com.lumi.app.data.models.tasks.TaskCompletionResponse
import com.lumi.app.data.models.tasks.TaskInput
import com.lumi.app.data.models.tasks.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskService {

    @GET("tasks")
    suspend fun getTasks(
        @Query("syncToken") syncToken: String? = null,
        @Query("syncRun") syncRun: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): PageResponse<TaskResponse>

    @POST("tasks")
    suspend fun createTask(
        @Body task: TaskInput
    ): TaskResponse

    // Devuelve el ETag necesario para actualizar
    @GET("tasks/{taskId}")
    suspend fun getTask(
        @Path("taskId") taskId: String
    ): Response<TaskResponse>

    @PUT("tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: String,
        @Header("If-Match") etag: String,
        @Body task: TaskInput
    ): Response<TaskResponse>

    @DELETE("tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: String
    ): Response<Unit>

    @PATCH("tasks/{taskId}/complete")
    suspend fun completeTask(
        @Path("taskId") taskId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CompleteTaskRequest
    ): TaskCompletionResponse

    @PATCH("tasks/{taskId}/reopen")
    suspend fun reopenTask(
        @Path("taskId") taskId: String,
        @Header("Idempotency-Key") idempotencyKey: String
    ): TaskCompletionResponse
}
