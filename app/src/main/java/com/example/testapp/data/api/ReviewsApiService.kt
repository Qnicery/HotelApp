package com.example.testapp.data.api

import com.example.testapp.data.api.model.HotelReviewStatsResponse
import com.example.testapp.data.api.model.ReviewCreateRequest
import com.example.testapp.data.api.model.ReviewDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API интерфейс для работы с отзывами
 * Base URL: http://localhost:8080
 * 
 * Документация: API_ROUTES.md - Reviews section
 */
interface ReviewsApiService {

    /**
     * Получить отзыв по ID
     * GET /reviews/{id}
     *
     * Response: 200 OK - ReviewDTO
     */
    @GET("reviews/{id}")
    suspend fun getReviewById(
        @Path("id") reviewId: Int
    ): Response<ReviewDTO>

    /**
     * Получить отзывы отеля со статистикой
     * GET /reviews/hotel/{hotelId}
     *
     * Response: 200 OK - HotelReviewStatsResponse
     * {
     *   "hotelId": 1,
     *   "reviewCount": 10,
     *   "averageRating": 4.5,
     *   "reviews": [...]
     * }
     */
    @GET("reviews/hotel/{hotelId}")
    suspend fun getReviewsByHotelId(
        @Path("hotelId") hotelId: Int
    ): Response<HotelReviewStatsResponse>

    /**
     * Создать отзыв
     * POST /reviews
     *
     * Request Body: ReviewCreateRequest
     * {
     *   "bookingId": 1,
     *   "hotelId": 1,
     *   "rating": 5,
     *   "text": "string or null"
     * }
     *
     * Response: 201 Created - ReviewDTO
     */
    @POST("reviews")
    suspend fun createReview(
        @Body request: ReviewCreateRequest
    ): Response<ReviewDTO>
}
