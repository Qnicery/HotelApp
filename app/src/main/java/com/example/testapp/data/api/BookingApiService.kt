package com.example.testapp.data.api

import com.example.testapp.data.api.model.AvailabilityResponse
import com.example.testapp.data.api.model.BookingCreateRequest
import com.example.testapp.data.api.model.BookingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * API интерфейс для работы с бронированиями
 * Base URL: http://localhost:8080
 * 
 * Документация: API_ROUTES.md - Bookings section
 */
interface BookingApiService {

    /**
     * Получить бронирования по ID пользователя
     * GET /bookings/user/{userId}
     *
     * Response: 200 OK - List<BookingDTO>
     */
    @GET("bookings/user/{userId}")
    suspend fun getBookingsByUserId(
        @Path("userId") userId: Int
    ): Response<List<BookingResponse>>

    /**
     * Получить бронирование по ID
     * GET /bookings/{id}
     *
     * Response: 200 OK - BookingDTO
     */
    @GET("bookings/{id}")
    suspend fun getBookingById(
        @Path("id") bookingId: Int
    ): Response<BookingResponse>

    /**
     * Создать бронирование
     * POST /bookings
     *
     * Request Body: BookingCreateRequest
     * {
     *   "userId": 1,
     *   "roomId": 1,
     *   "dateFrom": "2026-04-10T14:00:00Z",
     *   "dateTo": "2026-04-15T12:00:00Z"
     * }
     *
     * Response: 201 Created - BookingDTO
     */
    @POST("bookings")
    suspend fun createBooking(
        @Body request: BookingCreateRequest
    ): Response<BookingResponse>

    /**
     * Обновить статус бронирования
     * PUT /bookings/{id}/status
     *
     * Request Body: BookingStatusUpdateRequest
     * {
     *   "status": "Canceled"
     * }
     *
     * Response: 200 OK - BookingDTO
     */
    @PUT("bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") bookingId: Int,
        @Body request: BookingStatusUpdateRequest
    ): Response<BookingResponse>

    /**
     * Проверить доступность комнаты по датам
     * GET /bookings/room/{roomId}/availability?from={date}&to={date}
     *
     * Response: 200 OK - AvailabilityResponse
     * Возвращает isAvailable=true или список конфликтующих бронирований
     */
    @GET("bookings/room/{roomId}/availability")
    suspend fun checkRoomAvailability(
        @Path("roomId") roomId: Int,
        @Query("from") dateFrom: String,
        @Query("to") dateTo: String
    ): Response<AvailabilityResponse>
}

/**
 * Запрос на обновление статуса бронирования
 */
@kotlinx.serialization.Serializable
data class BookingStatusUpdateRequest(
    val status: String
)
