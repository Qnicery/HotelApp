package com.example.testapp.data.api

import com.example.testapp.data.api.model.AdminRequestDTO
import com.example.testapp.data.api.model.AdminRequestCreateRequest
import com.example.testapp.data.api.model.AdminRequestStatusUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * API интерфейс для работы с заявками на администратора
 * Base URL: http://10.0.2.2:8080
 */
interface AdminRequestsApiService {

    /**
     * Получить все заявки на администратора
     * GET /admin-requests
     *
     * Response: 200 OK - List<AdminRequestDTO>
     */
    @GET("admin-requests")
    suspend fun getAllAdminRequests(): Response<List<AdminRequestDTO>>

    /**
     * Получить заявки пользователя
     * GET /admin-requests/user/{userId}
     *
     * Response: 200 OK - List<AdminRequestDTO>
     */
    @GET("admin-requests/user/{userId}")
    suspend fun getAdminRequestsByUserId(
        @Path("userId") userId: Int
    ): Response<List<AdminRequestDTO>>

    /**
     * Создать заявку на администратора
     * POST /admin-requests
     *
     * Response: 201 Created - AdminRequestDTO
     */
    @POST("admin-requests")
    suspend fun createAdminRequest(
        @Body request: AdminRequestCreateRequest
    ): Response<AdminRequestDTO>

    /**
     * Обновить статус заявки
     * PUT /admin-requests/{id}/status
     *
     * Response: 200 OK - AdminRequestDTO
     */
    @PUT("admin-requests/{id}/status")
    suspend fun updateAdminRequestStatus(
        @Path("id") requestId: Int,
        @Body request: AdminRequestStatusUpdateRequest
    ): Response<AdminRequestDTO>
}
