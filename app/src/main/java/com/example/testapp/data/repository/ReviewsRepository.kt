package com.example.testapp.data.repository

import android.content.Context
import com.example.testapp.data.api.RetrofitClient
import com.example.testapp.data.api.model.HotelReviewStatsResponse
import com.example.testapp.data.api.model.ReviewCreateRequest
import com.example.testapp.data.api.model.ReviewDTO
import com.example.testapp.data.model.Review
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Репозиторий для работы с отзывами
 * Инкапсулирует работу с API: отзывы, статистика
 * 
 * Архитектура:
 * ReviewsScreen → ReviewsViewModel → ReviewsRepository → API
 */
class ReviewsRepository private constructor(context: Context) {

    private val reviewsApi = RetrofitClient.reviewsApi

    companion object {
        @Volatile
        private var instance: ReviewsRepository? = null

        fun getInstance(context: Context): ReviewsRepository {
            return instance ?: synchronized(this) {
                instance ?: ReviewsRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== Отзывы ====================

    /**
     * Получить отзывы отеля со статистикой
     * GET /reviews/hotel/{hotelId}
     * 
     * Response: HotelReviewStatsResponse { hotelId, reviewCount, averageRating, reviews }
     */
    suspend fun getReviewsByHotelId(hotelId: Int): Result<HotelReviewStatsResponse> {
        return try {
            val response = reviewsApi.getReviewsByHotelId(hotelId)

            if (response.isSuccessful) {
                val stats = response.body()
                if (stats != null) {
                    Result.success(stats)
                } else {
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Получить отзыв по ID
     * GET /reviews/{id}
     */
    suspend fun getReviewById(reviewId: Int): Result<ReviewDTO> {
        return try {
            val response = reviewsApi.getReviewById(reviewId)

            if (response.isSuccessful) {
                val reviewDTO = response.body()
                if (reviewDTO != null) {
                    Result.success(reviewDTO)
                } else {
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Создать отзыв
     * POST /reviews
     * 
     * Request: { bookingId, hotelId, rating, text }
     */
    suspend fun createReview(
        bookingId: Int,
        hotelId: Int,
        rating: Int,
        text: String? = null
    ): Result<Review> {
        return try {
            val request = ReviewCreateRequest(
                bookingId = bookingId,
                hotelId = hotelId,
                rating = rating,
                text = text
            )

            val response = reviewsApi.createReview(request)

            if (response.isSuccessful) {
                val reviewDTO = response.body()
                if (reviewDTO != null) {
                    Result.success(mapReviewDTOToReview(reviewDTO))
                } else {
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Ошибка сервера: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    // ==================== Маппинг моделей ====================

    /**
     * Конвертация ReviewDTO (сервер) → Review (приложение)
     * 
     * Сервер возвращает:
     * { id, bookingId, hotelId, rating, text, sentimentScore, createdAt }
     * 
     * Приложение ожидает:
     * { id, hotelId, userId, userName, userAvatarUrl, rating, text, date }
     * 
     * TODO: Когда сервер добавит userId, userName, userAvatarUrl — обновить маппинг
     */
    private fun mapReviewDTOToReview(dto: ReviewDTO): Review {
        // Форматируем дату
        val dateFormatted = try {
            val dateTime = LocalDateTime.parse(dto.createdAt)
            dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            dto.createdAt
        }

        return Review(
            id = dto.id,
            hotelId = dto.hotelId,
            userId = 0, // TODO: Когда сервер добавит userId — см. TODO.md
            userName = "Пользователь", // TODO: Когда сервер добавит userName — см. TODO.md
            userAvatarUrl = null, // TODO: Когда сервер добавит userAvatarUrl — см. TODO.md
            rating = dto.rating.toFloat(),
            text = dto.text ?: "",
            date = dateFormatted
        )
    }

    /**
     * Конвертация списка ReviewDTO → List<Review>
     */
    fun mapReviewDTOsToReviews(dtos: List<ReviewDTO>): List<Review> {
        return dtos.map { mapReviewDTOToReview(it) }
    }
}
