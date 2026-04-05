package com.example.testapp.data.api.model

import kotlinx.serialization.Serializable

/**
 * Ответ с информацией о бронировании (BookingDTO)
 * GET /bookings/user/{userId}
 * 
 * Согласно API_ROUTES.md:
 * {
 *   "id": 1,
 *   "userId": 1,
 *   "roomId": 1,
 *   "dateFrom": "ISO-8601 timestamp",
 *   "dateTo": "ISO-8601 timestamp",
 *   "status": "string",
 *   "createdAt": "ISO-8601 timestamp"
 * }
 */
@Serializable
data class BookingResponse(
    val id: Int,
    val userId: Int,
    val roomId: Int,
    val dateFrom: String,
    val dateTo: String,
    val status: String, // Active, Canceled, Completed
    val createdAt: String
)

/**
 * Запрос на создание бронирования
 * POST /bookings
 * 
 * {
 *   "userId": 1,
 *   "roomId": 1,
 *   "dateFrom": "2026-04-10T14:00:00Z",
 *   "dateTo": "2026-04-15T12:00:00Z"
 * }
 */
@Serializable
data class BookingCreateRequest(
    val userId: Int,
    val roomId: Int,
    val dateFrom: String,
    val dateTo: String
)
