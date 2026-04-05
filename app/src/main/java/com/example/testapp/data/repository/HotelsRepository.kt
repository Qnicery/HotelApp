package com.example.testapp.data.repository

import android.content.Context
import com.example.testapp.data.api.RetrofitClient
import com.example.testapp.data.api.ServerConfig
import com.example.testapp.data.api.model.HotelDTO
import com.example.testapp.data.api.model.RoomDTO
import com.example.testapp.data.model.City
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.HotelType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Репозиторий для работы с отелями
 * Инкапсулирует работу с API: отели, комнаты, города
 * 
 * Архитектура:
 * HomeScreen → HotelsViewModel → HotelsRepository → API
 */
class HotelsRepository private constructor(context: Context) {

    private val hotelsApi = RetrofitClient.hotelsApi

    companion object {
        @Volatile
        private var instance: HotelsRepository? = null

        fun getInstance(context: Context): HotelsRepository {
            return instance ?: synchronized(this) {
                instance ?: HotelsRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== Отели ====================

    /**
     * Получить все отели с сервера
     * GET /hotels
     * 
     * Для каждого отеля загружаются комнаты для вычисления минимальной цены
     */
    suspend fun getAllHotels(): Result<List<Hotel>> {
        return try {
            val response = hotelsApi.getAllHotels()

            if (response.isSuccessful) {
                val hotelDTOs = response.body() ?: emptyList()
                val hotels = hotelDTOs.map { dto -> mapHotelDTOToHotel(dto) }
                Result.success(hotels)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Получить отели по городу
     * GET /hotels/city/{city}
     */
    suspend fun getHotelsByCity(city: String): Result<List<Hotel>> {
        return try {
            val response = hotelsApi.getHotelsByCity(city)

            if (response.isSuccessful) {
                val hotelDTOs = response.body() ?: emptyList()
                val hotels = hotelDTOs.map { dto -> mapHotelDTOToHotel(dto) }
                Result.success(hotels)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Получить отель по ID
     * GET /hotels/{id}
     */
    suspend fun getHotelById(hotelId: Int): Result<Hotel> {
        return try {
            val response = hotelsApi.getHotelById(hotelId)

            if (response.isSuccessful) {
                val hotelDTO = response.body()
                if (hotelDTO != null) {
                    Result.success(mapHotelDTOToHotel(hotelDTO))
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

    // ==================== Комнаты ====================

    /**
     * Получить комнаты отеля
     * GET /rooms/hotel/{hotelId}
     */
    suspend fun getRoomsByHotelId(hotelId: Int): Result<List<RoomDTO>> {
        return try {
            val response = hotelsApi.getRoomsByHotelId(hotelId)

            if (response.isSuccessful) {
                val rooms = response.body() ?: emptyList()
                Result.success(rooms)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Получить минимальную цену номера для отеля
     * Если комнат нет — возвращает 0
     */
    suspend fun getMinPriceForHotel(hotelId: Int): Double {
        return try {
            val roomsResult = getRoomsByHotelId(hotelId)
            if (roomsResult.isSuccess) {
                val rooms = roomsResult.getOrNull() ?: emptyList()
                if (rooms.isEmpty()) 0.0 else rooms.minOf { it.price }
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    // ==================== Города ====================

    /**
     * Получить все уникальные города из отелей
     * Загружает все отели и извлекает уникальные города
     */
    suspend fun getCities(): Result<List<City>> {
        return try {
            val response = hotelsApi.getAllHotels()

            if (response.isSuccessful) {
                val hotelDTOs = response.body() ?: emptyList()
                val citiesMap = hotelDTOs
                    .groupBy { it.city }
                    .mapValues { it.value.size }
                    .map { City(name = it.key, hotelsCount = it.value) }
                    .sortedBy { it.name }

                Result.success(citiesMap)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    // ==================== Маппинг моделей ====================

    /**
     * Конвертация HotelDTO (сервер) → Hotel (приложение)
     * 
     * Сервер возвращает:
     * { id, adminId, name, city, description, address, rating, photoUrls }
     * 
     * Приложение ожидает:
     * { id, name, city, type, description, imageUrl, rating, reviewsCount, priceFrom, amenities, gallery }
     * 
     * priceFrom вычисляется из минимальной цены комнат
     * imageUrl и gallery — с префиксом BASE_URL
     */
    private suspend fun mapHotelDTOToHotel(dto: HotelDTO): Hotel {
        // Загружаем комнаты для вычисления минимальной цены
        val minPrice = getMinPriceForHotel(dto.id)

        // Пытаемся определить тип отеля из описания
        val hotelType = detectHotelType(dto.description)

        // Получаем URL изображений с префиксом сервера
        val imageUrl = ServerConfig.getImageUrl(dto.photoUrls?.firstOrNull())
        val gallery = ServerConfig.getImageUrls(dto.photoUrls)
        
        return Hotel(
            id = dto.id,
            name = dto.name,
            city = dto.city,
            type = hotelType,
            description = dto.description ?: "",
            imageUrl = imageUrl,
            rating = dto.rating.toFloat(),
            reviewsCount = 0, // TODO: Когда сервер добавит reviewsCount — см. TODO.md
            priceFrom = minPrice,
            amenities = emptyList(), // TODO: Загружать через GET /room-amenities — см. TODO.md
            gallery = gallery
        )
    }

    /**
     * Определить тип отеля из описания
     * TODO: Когда сервер добавит поле type — заменить на серверное значение
     */
    private fun detectHotelType(description: String?): HotelType {
        if (description.isNullOrBlank()) return HotelType.HOTEL

        return when {
            description.contains("курорт", ignoreCase = true) -> HotelType.RESORT
            description.contains("апартамент", ignoreCase = true) -> HotelType.APARTMENT
            description.contains("хостел", ignoreCase = true) -> HotelType.HOSTEL
            description.contains("гостев", ignoreCase = true) -> HotelType.GUEST_HOUSE
            description.contains("вилл", ignoreCase = true) -> HotelType.VILLA
            else -> HotelType.HOTEL
        }
    }
}
