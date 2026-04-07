package com.example.testapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.testapp.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Репозиторий для работы с данными
 * На текущем этапе использует заглушки (mock data)
 * 
 * Singleton pattern для обеспечения общего состояния между всеми ViewModel
 */
class AppRepository private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== SharedPreferences для сохранения пользователя ====================
    
    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "user_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun saveUserToPrefs(user: User?) {
        if (user != null) {
            val userJson = Json.encodeToString(UserSerializable(user))
            sharedPreferences.edit().putString("current_user", userJson).apply()
        } else {
            sharedPreferences.edit().remove("current_user").apply()
        }
    }

    private fun loadUserFromPrefs(): User? {
        return try {
            val userJson = sharedPreferences.getString("current_user", null)
            userJson?.let { Json.decodeFromString<UserSerializable>(it).toUser() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==================== Mock Data ====================

    private val users = mutableListOf(
        User(
            id = 1,
            name = "Иван Петров",
            email = "user@example.com",
            password = "123456",
            avatarUrl = null,
            role = UserRole.USER,
            registrationDate = "2024-01-01"
        ),
        User(
            id = 2,
            name = "Админ Отеля",
            email = "hotel@admin.com",
            password = "123456",
            avatarUrl = null,
            role = UserRole.HOTEL_ADMIN,
            registrationDate = "2024-01-05"
        ),
        User(
            id = 3,
            name = "Системный Админ",
            email = "system@admin.com",
            password = "123456",
            avatarUrl = null,
            role = UserRole.SYSTEM_ADMIN,
            registrationDate = "2024-01-10"
        )
    )

    // NOTE: Отели и города теперь загружаются с сервера через HotelsRepository
    // Mock-данные удалены, так как приложение использует реальный API
    private val hotelsList = listOf<Hotel>()
    private val cities = listOf<City>()

    private val roomsList = mutableListOf(
        // Номера для Grand Palace Hotel
        Room(
            id = 1,
            hotelId = 1,
            name = "Делюкс с видом на город",
            description = "Просторный номер площадью 45 м² с панорамными окнами и видом на Москву. Королевская кровать, рабочая зона, мраморная ванная комната.",
            imageUrl = "https://loremflickr.com/800/600/hotel,bedroom?random=1001",
            pricePerNight = 15000.0,
            maxGuests = 2,
            amenities = listOf("King-size кровать", "Мини-бар", "Сейф", "Кондиционер", "Балкон"),
            isAvailable = true
        ),
        Room(
            id = 2,
            hotelId = 1,
            name = "Представительский люкс",
            description = "Двухкомнатный номер площадью 80 м² с отдельной гостиной. Доступ в бизнес-лаунж и персональный консьерж-сервис.",
            imageUrl = "https://loremflickr.com/800/600/hotel,suite?random=1002",
            pricePerNight = 35000.0,
            maxGuests = 4,
            amenities = listOf("Гостиная", "King-size кровать", "Мини-бар", "Джакузи", "Балкон"),
            isAvailable = true
        ),
        // Номера для Seaside Resort
        Room(
            id = 3,
            hotelId = 2,
            name = "Стандарт с видом на море",
            description = "Уютный номер с балконом и видом на море. Идеально для пары. В номере есть всё необходимое для комфортного отдыха.",
            imageUrl = "https://loremflickr.com/800/600/bedroom,sea?random=2001",
            pricePerNight = 12000.0,
            maxGuests = 2,
            amenities = listOf("Балкон", "Вид на море", "Кондиционер", "Мини-бар"),
            isAvailable = true
        ),
        Room(
            id = 4,
            hotelId = 2,
            name = "Семейный номер",
            description = "Просторный номер для семьи с детьми. Две комнаты, детская зона, кухня. Вид на бассейн и море.",
            imageUrl = "https://loremflickr.com/800/600/family,room?random=2002",
            pricePerNight = 22000.0,
            maxGuests = 4,
            amenities = listOf("Кухня", "Две комнаты", "Балкон", "Вид на бассейн"),
            isAvailable = true
        ),
        // Номера для Mountain Lodge
        Room(
            id = 5,
            hotelId = 3,
            name = "Стандарт альпийский",
            description = "Тёплый номер в альпийском стиле с деревянной отделкой. Вид на горы, уютный балкон с креслами.",
            imageUrl = "https://loremflickr.com/800/600/bedroom,mountain?random=3001",
            pricePerNight = 9500.0,
            maxGuests = 2,
            amenities = listOf("Вид на горы", "Балкон", "Камин в номере", "Фен"),
            isAvailable = true
        ),
        Room(
            id = 6,
            hotelId = 3,
            name = "Шале люкс",
            description = "Отдельное шале с камином, сауной и панорамными окнами. Полная приватность и роскошь в горах.",
            imageUrl = "https://loremflickr.com/800/600/chalet,interior?random=3002",
            pricePerNight = 45000.0,
            maxGuests = 6,
            amenities = listOf("Сауна", "Камин", "Кухня", "Терраса", "Вид на горы"),
            isAvailable = true
        )
    )

    // NOTE: Отзывы теперь загружаются с сервера через ReviewsRepository
    // Mock-данные удалены, так как приложение использует реальный API
    private val reviewsList = listOf<Review>()

    private val bookingsList = mutableListOf<Booking>()

    private val notificationsList = mutableListOf(
        Notification(
            id = 1,
            userId = 1,
            title = "Бронирование подтверждено",
            message = "Ваше бронирование в Grand Palace Hotel подтверждено",
            type = NotificationType.BOOKING_CONFIRMED,
            isRead = false,
            createdAt = "2024-01-15 10:30"
        ),
        Notification(
            id = 2,
            userId = 1,
            title = "Напоминание о поездке",
            message = "Завтра ваш заезд в Seaside Resort",
            type = NotificationType.SYSTEM,
            isRead = true,
            createdAt = "2024-01-10 09:00"
        )
    )

    // ==================== State ====================

    private val _currentUser = MutableStateFlow<User?>(loadUserFromPrefs())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ==================== Auth ====================

    fun login(email: String, password: String): Result<User> {
        val user = users.find { it.email == email && it.password == password }
        return if (user != null) {
            _currentUser.value = user
            saveUserToPrefs(user)
            Result.success(user)
        } else {
            Result.failure(Exception("Неверный email или пароль"))
        }
    }

    fun register(name: String, email: String, password: String): Result<User> {
        if (users.any { it.email == email }) {
            return Result.failure(Exception("Пользователь с таким email уже существует"))
        }
        val newUser = User(
            id = users.maxOfOrNull { it.id }?.plus(1) ?: 1,
            name = name,
            email = email,
            password = password,
            role = UserRole.USER,
            registrationDate = java.time.LocalDate.now().toString()
        )
        users.add(newUser)
        _currentUser.value = newUser
        saveUserToPrefs(newUser)
        return Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
        saveUserToPrefs(null)
    }

    fun getCurrentUser(): User? = _currentUser.value

    // ==================== Hotels ====================

    fun getHotels(): List<Hotel> = hotelsList

    fun getHotelById(id: Int): Hotel? = hotelsList.find { it.id == id }

    fun getCities(): List<City> = cities

    fun searchHotels(params: SearchParams): List<Hotel> {
        return hotelsList.filter { hotel ->
            // Фильтр по городу
            (params.city == null || hotel.city == params.city) &&
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
    }

    /**
     * Поиск отелей с сортировкой
     */
    fun searchHotelsWithSort(params: SearchParams, sortOption: SortOption): List<Hotel> {
        val filtered = searchHotels(params)
        return when (sortOption) {
            SortOption.PRICE_ASC -> filtered.sortedBy { it.priceFrom }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.priceFrom }
            SortOption.RATING_DESC -> filtered.sortedByDescending { it.rating }
            SortOption.REVIEWS_DESC -> filtered.sortedByDescending { it.reviewsCount }
        }
    }

    /**
     * Получить все уникальные удобства из отелей
     */
    fun getAllAmenities(): List<String> {
        return hotelsList.flatMap { it.amenities }.distinct().sorted()
    }

    /**
     * Получить диапазон цен всех отелей
     */
    fun getPriceRange(): ClosedFloatingPointRange<Double> {
        val min = hotelsList.minOfOrNull { it.priceFrom } ?: 0.0
        val max = hotelsList.maxOfOrNull { it.priceFrom } ?: 0.0
        return min..max
    }

    /**
     * Получить все уникальные города из отелей
     */
    fun getAllCities(): List<String> {
        return hotelsList.map { it.city }.distinct().sorted()
    }

    // ==================== Rooms ====================

    fun getRoomsByHotel(hotelId: Int): List<Room> = roomsList.filter { it.hotelId == hotelId }

    fun getRoomById(id: Int): Room? = roomsList.find { it.id == id }

    // ==================== Reviews ====================

    // NOTE: Отзывы теперь загружаются с сервера через ReviewsRepository
    // fun getReviewsByHotel и fun addReview удалены

    // ==================== Bookings ====================

    fun createBooking(
        roomId: Int,
        userId: Int,
        dateFrom: String,
        dateTo: String,
        guests: Int,
        totalPrice: Double = 0.0
    ): Result<Booking> {
        val room = roomsList.find { it.id == roomId } ?: return Result.failure(Exception("Номер не найден"))
        val hotel = hotelsList.find { it.id == room.hotelId } ?: return Result.failure(Exception("Отель не найден"))

        // Расчёт количества ночей
        val from = java.time.LocalDate.parse(dateFrom)
        val to = java.time.LocalDate.parse(dateTo)
        val nights = java.time.temporal.ChronoUnit.DAYS.between(from, to).toInt().coerceAtLeast(1)

        // Используем переданный totalPrice или вычисляем
        val finalTotalPrice = if (totalPrice > 0) totalPrice else room.pricePerNight * nights

        val newBooking = Booking(
            id = bookingsList.maxOfOrNull { it.id }?.plus(1) ?: 1,
            roomId = roomId,
            hotelId = hotel.id,
            hotelName = hotel.name,
            roomName = room.name,
            userId = userId,
            dateFrom = dateFrom,
            dateTo = dateTo,
            guests = guests,
            totalPrice = finalTotalPrice,
            status = BookingStatus.CONFIRMED,
            createdAt = java.time.LocalDateTime.now().toString()
        )
        bookingsList.add(newBooking)
        return Result.success(newBooking)
    }

    fun getBookingsByUser(userId: Int): List<Booking> = bookingsList.filter { it.userId == userId }

    fun cancelBooking(bookingId: Int): Result<Unit> {
        val booking = bookingsList.find { it.id == bookingId }
            ?: return Result.failure(Exception("Бронирование не найдено"))

        val index = bookingsList.indexOfFirst { it.id == bookingId }
        bookingsList[index] = booking.copy(status = BookingStatus.CANCELLED)
        return Result.success(Unit)
    }

    fun getAllBookings(): List<Booking> = bookingsList

    // ==================== Notifications ====================

    fun getNotificationsByUser(userId: Int): List<Notification> =
        notificationsList.filter { it.userId == userId }

    fun markNotificationAsRead(notificationId: Int) {
        val index = notificationsList.indexOfFirst { it.id == notificationId }
        if (index >= 0) {
            notificationsList[index] = notificationsList[index].copy(isRead = true)
        }
    }

    // ==================== Admin ====================

    fun getAllUsers(): List<User> = users.toList()

    fun updateUserRole(userId: Int, newRole: UserRole): Result<Unit> {
        val user = users.find { it.id == userId }
            ?: return Result.failure(Exception("Пользователь не найден"))

        val index = users.indexOfFirst { it.id == userId }
        users[index] = user.copy(role = newRole)
        return Result.success(Unit)
    }

    fun getHotelsForAdmin(): List<Hotel> = hotelsList

    fun getRoomsForAdmin(hotelId: Int): List<Room> = roomsList.filter { it.hotelId == hotelId }

    fun updateRoomAvailability(roomId: Int, isAvailable: Boolean): Result<Unit> {
        val room = roomsList.find { it.id == roomId }
            ?: return Result.failure(Exception("Номер не найден"))

        val index = roomsList.indexOfFirst { it.id == roomId }
        roomsList[index] = room.copy(isAvailable = isAvailable)
        return Result.success(Unit)
    }
}

// ==================== Serializable Models для SharedPreferences ====================

@Serializable
data class UserSerializable(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val avatarUrl: String? = null,
    val role: UserRoleSerializable = UserRoleSerializable.USER,
    val registrationDate: String? = null
) {
    constructor(user: User) : this(
        id = user.id,
        name = user.name,
        email = user.email,
        password = user.password,
        avatarUrl = user.avatarUrl,
        role = UserRoleSerializable.fromUserRole(user.role),
        registrationDate = user.registrationDate
    )

    fun toUser(): User = User(
        id = id,
        name = name,
        email = email,
        password = password,
        avatarUrl = avatarUrl,
        role = role.toUserRole(),
        registrationDate = registrationDate
    )
}

@Serializable
enum class UserRoleSerializable {
    USER,
    HOTEL_ADMIN,
    SYSTEM_ADMIN;

    fun toUserRole(): UserRole = when (this) {
        USER -> UserRole.USER
        HOTEL_ADMIN -> UserRole.HOTEL_ADMIN
        SYSTEM_ADMIN -> UserRole.SYSTEM_ADMIN
    }

    companion object {
        fun fromUserRole(role: UserRole): UserRoleSerializable = when (role) {
            UserRole.USER -> USER
            UserRole.HOTEL_ADMIN -> HOTEL_ADMIN
            UserRole.SYSTEM_ADMIN -> SYSTEM_ADMIN
        }
    }
}
