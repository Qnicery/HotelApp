package com.example.testapp.data.repository

import android.content.Context
import com.example.testapp.data.api.RetrofitClient
import com.example.testapp.data.api.BookingStatusUpdateRequest
import com.example.testapp.data.api.model.BookingResponse
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.BookingStatus
import com.example.testapp.data.model.User
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Репозиторий для работы с профилем пользователя
 * Инкапсулирует работу с API: пользователь, бронирования
 * 
 * Архитектура:
 * ProfileScreen → ProfileViewModel → ProfileRepository → API
 */
class ProfileRepository private constructor(context: Context) {

    private val authApiRepository = AuthApiRepository.getInstance(context)
    private val bookingApi = RetrofitClient.bookingApi

    companion object {
        @Volatile
        private var instance: ProfileRepository? = null

        fun getInstance(context: Context): ProfileRepository {
            return instance ?: synchronized(this) {
                instance ?: ProfileRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== Пользователь ====================

    /**
     * Получить текущего пользователя с сервера (GET /me)
     * Возвращает актуальные данные, обновляет локальный кэш
     */
    suspend fun fetchCurrentUser(): Result<User> {
        return authApiRepository.fetchCurrentUser()
    }

    /**
     * Получить локально сохранённого пользователя (без запроса к серверу)
     */
    fun getCachedUser(): User? {
        return authApiRepository.getCurrentUser()
    }

    /**
     * Выйти из системы
     */
    suspend fun logout(): Result<Unit> {
        return authApiRepository.logout()
    }

    // ==================== Бронирования ====================

    /**
     * Получить бронирования пользователя с сервера
     * GET /bookings/user/{userId}
     * 
     * Возвращает список BookingDTO, конвертированный в модель приложения
     */
    suspend fun getBookingsByUserId(userId: Int): Result<List<Booking>> {
        return try {
            val response = bookingApi.getBookingsByUserId(userId)

            if (response.isSuccessful) {
                val bookingResponses = response.body() ?: emptyList()
                val bookings = bookingResponses.map { mapBookingResponseToBooking(it) }
                Result.success(bookings)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Получить одно бронирование по ID
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
     * Request: { userId, roomId, dateFrom, dateTo }
     */
    suspend fun createBooking(
        userId: Int,
        roomId: Int,
        dateFrom: String,
        dateTo: String
    ): Result<Booking> {
        return try {
            val request = com.example.testapp.data.api.model.BookingCreateRequest(
                userId = userId,
                roomId = roomId,
                dateFrom = dateFrom,
                dateTo = dateTo
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
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Отменить бронирование
     * PUT /bookings/{id}/status
     * { "status": "Canceled" }
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
     * 
     * Сервер возвращает:
     * { id, userId, roomId, dateFrom, dateTo, status, createdAt }
     * 
     * Приложение ожидает:
     * { id, roomId, hotelId, hotelName, roomName, userId, dateFrom, dateTo, guests, totalPrice, status, createdAt }
     * 
     * TODO: Когда сервер добавит hotelId, hotelName, roomName, guests, totalPrice —
     * обновить маппинг и модель BookingResponse
     */
    private fun mapBookingResponseToBooking(response: BookingResponse): Booking {
        // Преобразуем ISO-8601 timestamp в читаемую дату
        val dateFromFormatted = formatTimestamp(response.dateFrom, "yyyy-MM-dd")
        val dateToFormatted = formatTimestamp(response.dateTo, "yyyy-MM-dd")
        val createdAtFormatted = formatTimestamp(response.createdAt, "yyyy-MM-dd HH:mm")

        // Маппинг статуса сервера → статус приложения
        val status = when (response.status.lowercase()) {
            "active" -> BookingStatus.ACTIVE
            "canceled" -> BookingStatus.CANCELLED
            "completed" -> BookingStatus.COMPLETED
            else -> BookingStatus.PENDING
        }

        return Booking(
            id = response.id,
            roomId = response.roomId,
            hotelId = 0, // Сервер пока не возвращает hotelId
            hotelName = "Отель", // TODO: Получить через API когда будет готов
            roomName = "Номер #${response.roomId}", // TODO: Получить через API когда будет готов
            userId = response.userId,
            dateFrom = dateFromFormatted,
            dateTo = dateToFormatted,
            guests = 0, // Сервер пока не возвращает guests
            totalPrice = 0.0, // Сервер пока не возвращает totalPrice
            status = status,
            createdAt = createdAtFormatted
        )
    }

    /**
     * Форматирование ISO-8601 timestamp
     */
    private fun formatTimestamp(timestamp: String, pattern: String): String {
        return try {
            val offsetDateTime = OffsetDateTime.parse(timestamp)
            offsetDateTime.format(DateTimeFormatter.ofPattern(pattern))
        } catch (e: Exception) {
            // Если не удалось распарсить, возвращаем как есть
            timestamp
        }
    }
}
