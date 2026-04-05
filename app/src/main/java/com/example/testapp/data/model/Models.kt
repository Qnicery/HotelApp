package com.example.testapp.data.model

/**
 * Пользователь приложения
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String, // В реальном приложении хранить хэш
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.USER
)

/**
 * Роли пользователей
 */
enum class UserRole(val displayName: String) {
    USER("Пользователь"),
    HOTEL_ADMIN("Администратор отеля"),
    SYSTEM_ADMIN("Системный администратор")
}

/**
 * Отель
 */
data class Hotel(
    val id: Int,
    val name: String,
    val city: String,
    val type: HotelType,
    val description: String,
    val imageUrl: String,
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val priceFrom: Double = 0.0,
    val amenities: List<String> = emptyList(),
    val gallery: List<String> = emptyList()
)

/**
 * Тип жилья
 */
enum class HotelType(val displayName: String) {
    HOTEL("Отель"),
    RESORT("Курорт"),
    APARTMENT("Апартаменты"),
    HOSTEL("Хостел"),
    GUEST_HOUSE("Гостевой дом"),
    VILLA("Вилла")
}

/**
 * Номер в отеле
 */
data class Room(
    val id: Int,
    val hotelId: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val pricePerNight: Double,
    val maxGuests: Int,
    val amenities: List<String> = emptyList(),
    val isAvailable: Boolean = true
)

/**
 * Отзыв об отеле
 */
data class Review(
    val id: Int,
    val hotelId: Int,
    val userId: Int,
    val userName: String,
    val userAvatarUrl: String? = null,
    val rating: Float,
    val text: String,
    val date: String
)

/**
 * Бронирование
 */
data class Booking(
    val id: Int,
    val roomId: Int,
    val hotelId: Int,
    val hotelName: String,
    val roomName: String,
    val userId: Int,
    val dateFrom: String,
    val dateTo: String,
    val guests: Int,
    val totalPrice: Double,
    val status: BookingStatus,
    val createdAt: String
)

/**
 * Статус бронирования
 */
enum class BookingStatus(val displayName: String) {
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтверждено"),
    ACTIVE("Активно"),
    COMPLETED("Завершено"),
    CANCELLED("Отменено")
}

/**
 * Уведомление
 */
data class Notification(
    val id: Int,
    val userId: Int,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val createdAt: String
)

/**
 * Тип уведомления
 */
enum class NotificationType(val displayName: String) {
    BOOKING_CONFIRMED("Бронирование подтверждено"),
    BOOKING_CANCELLED("Бронирование отменено"),
    BOOKING_PENDING("Ожидает подтверждения"),
    REVIEW_REPLY("Ответ на отзыв"),
    SYSTEM("Системное")
}

/**
 * Параметры поиска отелей
 */
data class SearchParams(
    val city: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val guests: Int = 2,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val hotelTypes: List<HotelType> = emptyList(),
    val minRating: Float? = null,
    val minStars: Int? = null,
    val amenities: List<String> = emptyList()
)

/**
 * Опции сортировки результатов поиска
 */
enum class SortOption(val displayName: String) {
    PRICE_ASC("Цена: по возрастанию"),
    PRICE_DESC("Цена: по убыванию"),
    RATING_DESC("Рейтинг: по убыванию"),
    REVIEWS_DESC("Отзывы: по убыванию")
}

/**
 * Результат поиска с отфильтрованными и отсортированными отелями
 */
data class SearchResults(
    val hotels: List<Hotel>,
    val availableAmenities: List<String>,
    val availableCities: List<String>,
    val priceRange: ClosedFloatingPointRange<Double>,
    val totalResults: Int
)

/**
 * Город для фильтра
 */
data class City(
    val name: String,
    val hotelsCount: Int
)
