package com.example.testapp.data.api.model

import kotlinx.serialization.Serializable

/**
 * Запрос на регистрацию пользователя
 * POST /register
 */
@Serializable
data class UserCreateRequest(
    val email: String,
    val name: String,
    val password: String
)

/**
 * Запрос на авторизацию
 * POST /login
 */
@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String
)

/**
 * Ответ при успешной авторизации
 * POST /login - 200 OK
 * 
 * Сервер возвращает ТОЛЬКО токен:
 * {"token":"632ec902-8139-4b59-a566-2b5698d0feae"}
 */
@Serializable
data class UserLoginResponse(
    val token: String
)

/**
 * Альтернативный ответ (если сервер будет возвращать с данными пользователя)
 * {"token":"...", "user":{...}}
 */
@Serializable
data class UserLoginResponseWithUser(
    val token: String,
    val user: UserResponse
)

/**
 * Ответ с информацией о пользователе
 * GET /me, POST /register
 */
@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val name: String,
    val role: String
)

/**
 * Запрос на обновление роли пользователя
 * PUT /users/{id}/role
 */
@Serializable
data class UserRoleUpdateRequest(
    val role: String
)
