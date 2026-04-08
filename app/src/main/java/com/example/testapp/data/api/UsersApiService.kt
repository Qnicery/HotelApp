package com.example.testapp.data.api

import com.example.testapp.data.api.model.UserResponse
import com.example.testapp.data.api.model.UserRoleUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * API интерфейс для работы с пользователями
 * Base URL: http://10.0.2.2:8080
 */
interface UsersApiService {

    /**
     * Получить всех пользователей
     * GET /users
     *
     * Response: 200 OK - List<UserResponse>
     */
    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    /**
     * Обновить роль пользователя
     * PUT /users/{id}/role
     *
     * Response: 200 OK - UserResponse
     */
    @PUT("users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") userId: Int,
        @Body request: UserRoleUpdateRequest
    ): Response<UserResponse>
}
