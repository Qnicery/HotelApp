package com.example.testapp.data.api.model

import kotlinx.serialization.Serializable

/**
 * Ответ с информацией об отеле (HotelDTO)
 * GET /hotels
 *
 * Согласно API_ROUTES.md + сервер реально возвращает:
 * {
 *   "id": 1,
 *   "adminId": 1,
 *   "name": "string",
 *   "city": "string",
 *   "description": "string or null",
 *   "address": "string",
 *   "rating": 0.0,
 *   "type": "Гостиница",
 *   "photoUrls": ["uploads/hotels/1/photo1.jpg", "uploads/hotels/1/photo2.jpg"]
 * }
 */
@Serializable
data class HotelDTO(
    val id: Int,
    val adminId: Int,
    val name: String,
    val city: String,
    val description: String? = null,
    val address: String,
    val rating: Double = 0.0,
    val type: String? = null,
    val photoUrls: List<String>? = null
)

/**
 * Ответ с информацией о комнате (RoomDTO)
 * GET /rooms/hotel/{hotelId}
 *
 * Согласно API_ROUTES.md + сервер реально возвращает photoUrls:
 * {
 *   "id": 1,
 *   "hotelId": 1,
 *   "roomName": "string",
 *   "description": "string or null",
 *   "price": 0.0,
 *   "maxGuests": 2,
 *   "status": "string",
 *   "photoUrls": ["uploads/rooms/1/photo1.jpg"]
 * }
 */
@Serializable
data class RoomDTO(
    val id: Int,
    val hotelId: Int,
    val roomName: String,
    val description: String? = null,
    val price: Double,
    val maxGuests: Int,
    val status: String,
    val photoUrls: List<String>? = null
)

/**
 * Удобство (AmenityDTO)
 * GET /amenities, GET /room-amenities/room/{roomId}, GET /room-amenities/hotel/{hotelId}
 *
 * {
 *   "id": 1,
 *   "name": "WiFi"
 * }
 */
@Serializable
data class AmenityDTO(
    val id: Int,
    val name: String
)
