package com.example.testapp.data.api

import com.example.testapp.data.api.model.UserCreateRequest
import com.example.testapp.data.api.model.UserLoginRequest
import com.example.testapp.data.api.model.UserLoginResponse
import com.example.testapp.data.api.model.UserResponse
import com.example.testapp.data.api.model.UserRoleUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * API интерфейс для работы с авторизацией
 * Base URL: http://localhost:8080
 */
interface AuthApiService {

    /**
     * Регистрация пользователя
     * POST /register
     * 
     * Response: 201 Created - UserResponse
     */
    @POST("register")
    suspend fun register(
        @Body request: UserCreateRequest
    ): Response<UserResponse>

    /**
     * Авторизация пользователя
     * POST /login
     * 
     * Response: 200 OK - UserLoginResponse с токеном
     */
    @POST("login")
    suspend fun login(
        @Body request: UserLoginRequest
    ): Response<UserLoginResponse>

    /**
     * Выход из системы
     * POST /logout
     * 
     * Header: Authorization: Bearer <token>
     * Response: 200 OK
     */
    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>

    /**
     * Получить текущего пользователя
     * GET /me
     * 
     * Header: Authorization: Bearer <token>
     * Response: 200 OK - UserResponse
     */
    @GET("me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>

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
