package com.example.testapp.data.repository

import android.content.Context
import com.example.testapp.data.api.AmenitiesApiService
import com.example.testapp.data.api.RetrofitClient
import com.example.testapp.data.api.ServerConfig
import com.example.testapp.data.api.model.AvailableRoomsResponse
import com.example.testapp.data.api.model.HotelDTO
import com.example.testapp.data.api.model.RoomDTO
import com.example.testapp.data.model.City
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.HotelType
import com.example.testapp.data.model.SearchParams
import com.example.testapp.data.model.SortOption
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
    private val amenitiesApi = RetrofitClient.amenitiesApi
    private val reviewsRepository = ReviewsRepository.getInstance(context)

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
     * Получить доступные комнаты отеля по датам
     * GET /rooms/hotel/{hotelId}/available?from={date}&to={date}
     */
    suspend fun getAvailableRoomsByHotelId(
        hotelId: Int,
        dateFrom: String,
        dateTo: String
    ): Result<AvailableRoomsResponse> {
        return try {
            val response = hotelsApi.getAvailableRoomsByHotelId(hotelId, dateFrom, dateTo)

            if (response.isSuccessful) {
                val availableRooms = response.body()
                if (availableRooms != null) {
                    Result.success(availableRooms)
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

    // ==================== Удобства ====================

    /**
     * Получить все удобства (из всей системы)
     * GET /amenities
     */
    suspend fun getAllAmenities(): Result<List<String>> {
        return try {
            val response = amenitiesApi.getAllAmenities()

            if (response.isSuccessful) {
                val amenities = response.body() ?: emptyList()
                Result.success(amenities.map { it.name }.sorted())
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Получить все уникальные удобства отеля
     * GET /room-amenities/hotel/{hotelId}
     */
    suspend fun getHotelAmenities(hotelId: Int): Result<List<String>> {
        return try {
            val response = amenitiesApi.getAmenitiesByHotelId(hotelId)

            if (response.isSuccessful) {
                val amenities = response.body() ?: emptyList()
                Result.success(amenities.map { it.name })
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
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

    // ==================== Поиск и фильтрация ====================

    /**
     * Поиск отелей с параметрами и сортировкой
     * Загружает все отели и фильтрует на клиенте
     */
    suspend fun searchHotels(params: SearchParams): Result<List<Hotel>> {
        return try {
            val allHotelsResult = getAllHotels()
            if (allHotelsResult.isFailure) {
                return Result.failure(allHotelsResult.exceptionOrNull() ?: Exception("Ошибка загрузки отелей"))
            }

            val hotels = allHotelsResult.getOrNull() ?: emptyList()
            val filtered = hotels.filter { hotel ->
                // Фильтр по городу
                (params.city == null || hotel.city.equals(params.city, ignoreCase = true)) &&
                // Фильтр по типу
                (params.hotelTypes.isEmpty() || hotel.type in params.hotelTypes) &&
                // Фильтр по цене
                (params.priceMin == null || hotel.priceFrom >= params.priceMin) &&
                (params.priceMax == null || hotel.priceFrom <= params.priceMax) &&
                // Фильтр по рейтингу
                (params.minRating == null || hotel.rating >= params.minRating) &&
                // Фильтр по удобствам
                (params.amenities.isEmpty() || params.amenities.all { it in hotel.amenities })
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка поиска: ${e.message}"))
        }
    }

    /**
     * Поиск отелей с сортировкой
     * Если указаны даты — фильтрует отели, у которых нет доступных комнат
     */
    suspend fun searchHotelsWithSort(params: SearchParams, sortOption: SortOption): Result<List<Hotel>> {
        val searchResult = searchHotels(params)
        if (searchResult.isFailure) {
            return Result.failure(searchResult.exceptionOrNull() ?: Exception("Ошибка поиска"))
        }

        var hotels = searchResult.getOrNull() ?: emptyList()

        // Если указаны даты — фильтруем отели по доступности комнат
        val dateFrom = params.checkInDate
        val dateTo = params.checkOutDate
        if (dateFrom != null && dateTo != null) {
            // Форматируем даты в ISO-8601 для API
            val apiDateFrom = "${dateFrom}T14:00:00Z"
            val apiDateTo = "${dateTo}T12:00:00Z"

            val hotelsWithRooms = mutableListOf<Hotel>()
            for (hotel in hotels) {
                val availableResult = getAvailableRoomsByHotelId(hotel.id, apiDateFrom, apiDateTo)
                if (availableResult.isSuccess) {
                    val availableRooms = availableResult.getOrNull()?.availableRooms
                    if (!availableRooms.isNullOrEmpty()) {
                        // Обновляем цену отеля по минимальной доступной комнате
                        val minAvailablePrice = availableRooms.minOf { it.price }
                        hotelsWithRooms.add(hotel.copy(priceFrom = minAvailablePrice))
                    }
                }
            }
            hotels = hotelsWithRooms
        }

        val sorted = when (sortOption) {
            SortOption.PRICE_ASC -> hotels.sortedBy { it.priceFrom }
            SortOption.PRICE_DESC -> hotels.sortedByDescending { it.priceFrom }
            SortOption.RATING_DESC -> hotels.sortedByDescending { it.rating }
            SortOption.REVIEWS_DESC -> hotels.sortedByDescending { it.reviewsCount }
        }
        return Result.success(sorted)
    }

    // ==================== Маппинг моделей ====================

    /**
     * Конвертация HotelDTO (сервер) → Hotel (приложение)
     *
     * Сервер возвращает:
     * { id, adminId, name, city, description, address, rating, type, photoUrls }
     *
     * Приложение ожидает:
     * { id, name, city, type, description, imageUrl, rating, reviewsCount, priceFrom, amenities, gallery }
     *
     * priceFrom вычисляется из минимальной цены комнат
     * imageUrl и gallery — с префиксом BASE_URL
     * reviewsCount загружается из GET /reviews/hotel/{hotelId}
     * amenities загружается из GET /room-amenities/hotel/{hotelId}
     * type берётся с сервера (если есть) или определяется из описания
     */
    private suspend fun mapHotelDTOToHotel(dto: HotelDTO): Hotel {
        // Загружаем комнаты для вычисления минимальной цены
        val minPrice = getMinPriceForHotel(dto.id)

        // Определяем тип отеля: серверный или fallback из описания
        val hotelType = mapHotelType(dto.type) ?: detectHotelType(dto.description)

        // Получаем URL изображений с префиксом сервера
        val imageUrl = ServerConfig.getImageUrl(dto.photoUrls?.firstOrNull())
        val gallery = ServerConfig.getImageUrls(dto.photoUrls)

        // Загружаем количество отзывов
        val reviewsCount = try {
            val statsResult = reviewsRepository.getReviewsByHotelId(dto.id)
            if (statsResult.isSuccess) {
                statsResult.getOrNull()?.reviewCount ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }

        // Загружаем удобства отеля
        val amenities = try {
            val amenitiesResult = getHotelAmenities(dto.id)
            if (amenitiesResult.isSuccess) {
                amenitiesResult.getOrNull() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }

        return Hotel(
            id = dto.id,
            name = dto.name,
            city = dto.city,
            type = hotelType,
            description = dto.description ?: "",
            imageUrl = imageUrl,
            rating = dto.rating.toFloat(),
            reviewsCount = reviewsCount,
            priceFrom = minPrice,
            amenities = amenities,
            gallery = gallery
        )
    }

    /**
     * Конвертировать строку типа отеля с сервера в HotelType
     * Сервер возвращает: "Гостиница", "Курорт", "Апартаменты", "Хостел", "Гостевой дом", "Вилла"
     */
    private fun mapHotelType(typeString: String?): HotelType? {
        if (typeString.isNullOrBlank()) return null

        return when {
            typeString.contains("гостиниц", ignoreCase = true) -> HotelType.HOTEL
            typeString.contains("курорт", ignoreCase = true) -> HotelType.RESORT
            typeString.contains("апартамент", ignoreCase = true) -> HotelType.APARTMENT
            typeString.contains("хостел", ignoreCase = true) -> HotelType.HOSTEL
            typeString.contains("гостев", ignoreCase = true) -> HotelType.GUEST_HOUSE
            typeString.contains("вилл", ignoreCase = true) -> HotelType.VILLA
            else -> null
        }
    }

    /**
     * Определить тип отеля из описания (fallback)
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
