package com.example.testapp.data.api.model

import kotlinx.serialization.Serializable

/**
 * Ответ с информацией об административной заявке
 * GET /admin-requests
 */
@Serializable
data class AdminRequestDTO(
    val id: Int,
    val userId: Int,
    val text: String,
    val status: String,
    val createdAt: String
)

/**
 * Запрос на создание административной заявки
 * POST /admin-requests
 */
@Serializable
data class AdminRequestCreateRequest(
    val userId: Int,
    val text: String
)

/**
 * Запрос на обновление статуса административной заявки
 * PUT /admin-requests/{id}/status
 */
@Serializable
data class AdminRequestStatusUpdateRequest(
    val status: String
)
