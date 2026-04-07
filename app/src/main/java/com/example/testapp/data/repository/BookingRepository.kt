package com.example.testapp.data.repository

import android.content.Context
import com.example.testapp.data.api.BookingApiService
import com.example.testapp.data.api.BookingStatusUpdateRequest
import com.example.testapp.data.api.RetrofitClient
import com.example.testapp.data.api.model.AvailabilityResponse
import com.example.testapp.data.api.model.BookingCreateRequest
import com.example.testapp.data.api.model.BookingResponse
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.BookingStatus

/**
 * Репозиторий для работы с бронированиями через API
 *
 * Архитектура:
 * BookingViewModel → BookingRepository → BookingApiService → Backend
 */
class BookingRepository private constructor(context: Context) {

    private val bookingApi: BookingApiService = RetrofitClient.bookingApi
    private val hotelsRepository = HotelsRepository.getInstance(context)

    companion object {
        @Volatile
        private var instance: BookingRepository? = null

        fun getInstance(context: Context): BookingRepository {
            return instance ?: synchronized(this) {
                instance ?: BookingRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== Бронирования ====================

    /**
     * Получить бронирования пользователя
     * GET /bookings/user/{userId}
     */
    suspend fun getBookingsByUserId(userId: Int): Result<List<Booking>> {
        return try {
            val response = bookingApi.getBookingsByUserId(userId)

            if (response.isSuccessful) {
                val bookingResponses = response.body() ?: emptyList()
                Result.success(bookingResponses.map { mapBookingResponseToBooking(it) })
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Получить бронирование по ID
     * GET /bookings/{id}
     */
    suspend fun getBookingById(bookingId: Int): Result<Booking> {
        return try {
            val response = bookingApi.getBookingById(bookingId)

            if (response.isSuccessful) {
                val bookingResponse = response.body()
                if (bookingResponse != null) {
                    Result.success(mapBookingResponseToBooking(bookingResponse))
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
     * Создать бронирование
     * POST /bookings
     *
     * Request: { userId, roomId, dateFrom, dateTo, guests, totalPrice }
     */
    suspend fun createBooking(
        userId: Int,
        roomId: Int,
        dateFrom: String,
        dateTo: String,
        guests: Int,
        totalPrice: Double
    ): Result<Booking> {
        return try {
            val request = BookingCreateRequest(
                userId = userId,
                roomId = roomId,
                dateFrom = dateFrom,
                dateTo = dateTo,
                guests = guests,
                totalPrice = totalPrice
            )

            val response = bookingApi.createBooking(request)

            if (response.isSuccessful) {
                val bookingResponse = response.body()
                if (bookingResponse != null) {
                    Result.success(mapBookingResponseToBooking(bookingResponse))
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

    /**
     * Проверить доступность комнаты по датам
     * GET /bookings/room/{roomId}/availability?from={date}&to={date}
     */
    suspend fun checkRoomAvailability(
        roomId: Int,
        dateFrom: String,
        dateTo: String
    ): Result<AvailabilityResponse> {
        return try {
            val response = bookingApi.checkRoomAvailability(roomId, dateFrom, dateTo)

            if (response.isSuccessful) {
                val availability = response.body()
                if (availability != null) {
                    Result.success(availability)
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
     * Отменить бронирование
     * PUT /bookings/{id}/status
     */
    suspend fun cancelBooking(bookingId: Int): Result<Booking> {
        return try {
            val response = bookingApi.updateBookingStatus(
                bookingId = bookingId,
                request = BookingStatusUpdateRequest("Canceled")
            )

            if (response.isSuccessful) {
                val bookingResponse = response.body()
                if (bookingResponse != null) {
                    Result.success(mapBookingResponseToBooking(bookingResponse))
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

    // ==================== Маппинг моделей ====================

    /**
     * Конвертация BookingResponse (сервер) → Booking (приложение)
     */
    private suspend fun mapBookingResponseToBooking(response: BookingResponse): Booking {
        val status = when (response.status.lowercase()) {
            "active" -> BookingStatus.ACTIVE
            "canceled" -> BookingStatus.CANCELLED
            "completed" -> BookingStatus.COMPLETED
            else -> BookingStatus.PENDING
        }

        // Загружаем название отеля по hotelId
        val hotelName = try {
            val hotelResult = hotelsRepository.getHotelById(response.hotelId)
            hotelResult.getOrNull()?.name ?: "Отель #${response.hotelId}"
        } catch (e: Exception) {
            "Отель #${response.hotelId}"
        }

        // Загружаем название комнаты по roomId
        val roomName = try {
            val roomsResult = hotelsRepository.getRoomsByHotelId(response.hotelId)
            if (roomsResult.isSuccess) {
                val rooms = roomsResult.getOrNull() ?: emptyList()
                rooms.find { it.id == response.roomId }?.roomName ?: "Номер #${response.roomId}"
            } else {
                "Номер #${response.roomId}"
            }
        } catch (e: Exception) {
            "Номер #${response.roomId}"
        }

        return Booking(
            id = response.id,
            roomId = response.roomId,
            hotelId = response.hotelId,
            hotelName = hotelName,
            roomName = roomName,
            userId = response.userId,
            dateFrom = response.dateFrom,
            dateTo = response.dateTo,
            guests = response.guests,
            totalPrice = response.totalPrice,
            status = status,
            createdAt = response.createdAt
        )
    }
}
