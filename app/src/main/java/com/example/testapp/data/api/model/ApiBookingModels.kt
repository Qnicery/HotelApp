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
 *   "hotelId": 1,
 *   "dateFrom": "ISO-8601 timestamp",
 *   "dateTo": "ISO-8601 timestamp",
 *   "guests": 2,
 *   "totalPrice": 500.0,
 *   "status": "string",
 *   "createdAt": "ISO-8601 timestamp"
 * }
 */
@Serializable
data class BookingResponse(
    val id: Int,
    val userId: Int,
    val roomId: Int,
    val hotelId: Int,
    val dateFrom: String,
    val dateTo: String,
    val guests: Int,
    val totalPrice: Double,
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
 *   "dateTo": "2026-04-15T12:00:00Z",
 *   "guests": 2,
 *   "totalPrice": 500.0
 * }
 */
@Serializable
data class BookingCreateRequest(
    val userId: Int,
    val roomId: Int,
    val dateFrom: String,
    val dateTo: String,
    val guests: Int,
    val totalPrice: Double
)

/**
 * Информация о конфликтном бронировании
 * Ответ от GET /bookings/room/{roomId}/availability
 */
@Serializable
data class ConflictingBooking(
    val bookingId: Int,
    val dateFrom: String,
    val dateTo: String,
    val status: String
)

/**
 * Ответ проверки доступности комнаты
 * GET /bookings/room/{roomId}/availability?from={date}&to={date}
 */
@Serializable
data class AvailabilityResponse(
    val roomId: Int,
    val from: String,
    val to: String,
    val isAvailable: Boolean,
    val conflictingBookings: List<ConflictingBooking>
)

/**
 * Информация о доступной комнате
 * Ответ от GET /rooms/hotel/{hotelId}/available?from={date}&to={date}
 */
@Serializable
data class AvailableRoomInfo(
    val roomId: Int,
    val roomName: String,
    val description: String?,
    val price: Double,
    val maxGuests: Int,
    val photoUrls: List<String>
)

/**
 * Ответ с доступными комнатами отеля
 * GET /rooms/hotel/{hotelId}/available?from={date}&to={date}
 */
@Serializable
data class AvailableRoomsResponse(
    val hotelId: Int,
    val from: String,
    val to: String,
    val availableRooms: List<AvailableRoomInfo>
)
