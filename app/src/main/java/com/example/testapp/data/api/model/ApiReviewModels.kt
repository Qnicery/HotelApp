package com.example.testapp.data.api.model

import kotlinx.serialization.Serializable

/**
 * Ответ с информацией об отзыве (ReviewDTO)
 * GET /reviews/hotel/{hotelId}
 * 
 * Согласно API_ROUTES.md:
 * {
 *   "id": 1,
 *   "bookingId": 5,
 *   "hotelId": 1,
 *   "rating": 5,
 *   "text": "string or null",
 *   "sentimentScore": 5.0,
 *   "createdAt": "ISO-8601 timestamp"
 * }
 */
@Serializable
data class ReviewDTO(
    val id: Int,
    val bookingId: Int,
    val hotelId: Int,
    val rating: Int,
    val text: String? = null,
    val sentimentScore: Double,
    val createdAt: String
)

/**
 * Ответ со статистикой отзывов отеля
 * GET /reviews/hotel/{hotelId}
 * 
 * Согласно API_ROUTES.md:
 * {
 *   "hotelId": 1,
 *   "reviewCount": 10,
 *   "averageRating": 4.5,
 *   "reviews": [...]
 * }
 */
@Serializable
data class HotelReviewStatsResponse(
    val hotelId: Int,
    val reviewCount: Int,
    val averageRating: Double,
    val reviews: List<ReviewDTO>
)

/**
 * Запрос на создание отзыва
 * POST /reviews
 * 
 * Согласно API_ROUTES.md:
 * {
 *   "bookingId": 1,
 *   "hotelId": 1,
 *   "rating": 5,
 *   "text": "string or null"
 * }
 */
@Serializable
data class ReviewCreateRequest(
    val bookingId: Int,
    val hotelId: Int,
    val rating: Int,
    val text: String? = null
)
