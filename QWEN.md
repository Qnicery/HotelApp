# HotelApp — Android-приложение для бронирования отелей

## Обзор проекта

**HotelApp** — это Android-приложение для поиска, просмотра и бронирования отелей, разработанное с использованием современных технологий Android-разработки:

- **Kotlin** — основной язык разработки
- **Jetpack Compose** — декларативный UI-фреймворк
- **Material 3** — дизайн-система
- **MVVM архитектура** — разделение логики и UI
- **Navigation Compose** — навигация между экранами с поддержкой графов
- **Gradle Kotlin DSL + Version Catalog** — современная система сборки
- **Coil** — загрузка изображений
- **Repository Pattern** — абстракция доступа к данным (mock data)
- **Kotlinx Serialization** — работа с JSON
- **Encrypted SharedPreferences** — безопасное хранение сессии пользователя

**Package:** `com.example.testapp`  
**Application ID:** `com.example.testapp`  
**Version:** 1.0 (versionCode: 1)

---

## Структура проекта

```
TestApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/testapp/
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   │   └── Models.kt              # Модели: User, Hotel, Room, Review, Booking...
│   │   │   │   └── repository/
│   │   │   │       └── AppRepository.kt       # Репозиторий с mock-данными
│   │   │   ├── ui/
│   │   │   │   ├── components/                # Переиспользуемые UI-компоненты
│   │   │   │   │   ├── DateRangePicker.kt     # Кастомный выбор диапазона дат
│   │   │   │   │   └── RangeSlider.kt         # Двухпозиционный ползунок
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── Screen.kt              # sealed class навигации
│   │   │   │   │   └── AppNavigation.kt       # Граф навигации с поддержкой ролей
│   │   │   │   ├── screens/
│   │   │   │   │   ├── SplashScreen.kt        # Экран приветствия
│   │   │   │   │   ├── LoginScreen.kt         # Экран авторизации
│   │   │   │   │   ├── RegisterScreen.kt      # Экран регистрации
│   │   │   │   │   ├── HomeScreen.kt          # Главный экран со списком отелей
│   │   │   │   │   ├── SearchScreen.kt        # Экран поиска
│   │   │   │   │   ├── SearchResultsScreen.kt # Результаты поиска
│   │   │   │   │   ├── HotelDetailsScreen.kt  # Детали отеля
│   │   │   │   │   ├── RoomsListScreen.kt     # Список номеров
│   │   │   │   │   ├── BookingScreen.kt       # Бронирование
│   │   │   │   │   ├── ProfileScreen.kt       # Профиль пользователя
│   │   │   │   │   ├── BookingHistoryScreen.kt# История бронирований
│   │   │   │   │   ├── ReviewsScreen.kt       # Отзывы
│   │   │   │   │   ├── NotificationsScreen.kt # Уведомления
│   │   │   │   │   ├── SettingsScreen.kt      # Настройки
│   │   │   │   │   └── admin/                 # Экраны администратора
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt               # Цветовая палитра (синяя/океан)
│   │   │   │   │   ├── Theme.kt               # Тема приложения
│   │   │   │   │   └── Type.kt                # Типографика
│   │   │   │   └── viewmodel/
│   │   │   │       ├── AuthViewModel.kt       # ViewModel авторизации
│   │   │   │       ├── HotelsViewModel.kt     # ViewModel списка отелей
│   │   │   │       ├── HotelDetailsViewModel.kt
│   │   │   │       ├── BookingViewModel.kt
│   │   │   │       ├── ProfileViewModel.kt
│   │   │   │       ├── ReviewsViewModel.kt
│   │   │   │       ├── SearchViewModel.kt     # ViewModel поиска
│   │   │   │       └── AdminViewModel.kt
│   │   │   └── MainActivity.kt                # Главная Activity
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml                # Строки (app_name = HotelApp)
│   │   │   │   ├── colors.xml                 # Цвета
│   │   │   │   └── themes.xml                 # Тема приложения
│   │   │   └── ...                            # Drawable, mipmap, xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml                     # Version catalog зависимостей
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── project.md                                 # Спецификация приложения
├── USER_FLOW.md                               # Пользовательские сценарии
├── SEARCH_FEATURE.md                          # Документация поиска
├── API_ROUTES.md                              # Документация backend API
└── QWEN.md                                    # Этот файл
```

---

## Архитектура приложения

Приложение построено по архитектуре **MVVM (Model-View-ViewModel)** с использованием **Repository Pattern**:

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Screens)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │  Splash  │  │  Login   │  │   Home   │  │ Profile │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                  ViewModel Layer                         │
│  ┌──────────────────┐    ┌──────────────────────────┐   │
│  │  AuthViewModel   │    │   HotelsViewModel        │   │
│  │  (StateFlow)     │    │   (StateFlow)            │   │
│  └──────────────────┘    └──────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │              AppRepository.kt                    │   │
│  │  - Mock data (users, hotels, rooms, bookings)    │   │
│  │  - StateFlow currentUser                         │   │
│  │  - CRUD операции                                 │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Models.kt: User, Hotel, Room, Review, Booking   │   │
│  │  Enums: UserRole, HotelType, BookingStatus...    │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## Роли пользователей

Приложение поддерживает три роли с разными правами доступа:

| Роль | Описание | Стартовый экран |
|------|----------|-----------------|
| `USER` | Обычный пользователь | Home (список отелей) |
| `HOTEL_ADMIN` | Администратор отеля | AdminHotelDashboard |
| `SYSTEM_ADMIN` | Системный администратор | AdminSystemDashboard |

### Возможности ролей

**Пользователь (USER):**
- Поиск и просмотр отелей
- Бронирование номеров
- Просмотр истории бронирований
- Написание отзывов
- Управление профилем

**Администратор отеля (HOTEL_ADMIN):**
- Управление отелем
- Управление номерами (доступность)
- Просмотр бронирований
- Просмотр отзывов

**Системный администратор (SYSTEM_ADMIN):**
- Управление пользователями
- Назначение ролей
- Обработка заявок на администратора

---

## Экраны приложения

### Auth Graph (Неавторизованный пользователь)

| Экран | Описание |
|-------|----------|
| `Splash` | Приветственный экран с логотипом и кнопками входа/регистрации |
| `Login` | Авторизация (email + пароль) |
| `Register` | Регистрация (имя + email + пароль) |

### Main Graph (Пользователь)

| Экран | Описание |
|-------|----------|
| `Home` | Список отелей с фильтрами по городам |
| `Search` | Расширенный поиск с параметрами |
| `SearchResults` | Результаты поиска с фильтрами и сортировкой |
| `HotelDetails` | Детальная информация об отеле |
| `RoomsList` | Список номеров отеля |
| `Booking` | Оформление бронирования |
| `Profile` | Профиль пользователя |
| `BookingHistory` | История бронирований (активные/завершённые) |
| `Reviews` | Отзывы об отеле |
| `Notifications` | Уведомления |
| `Settings` | Настройки |

### Admin Hotel Graph

| Экран | Описание |
|-------|----------|
| `AdminHotelDashboard` | Панель управления отелем |
| `AdminRoomsList` | Управление номерами |
| `AdminHotelBookings` | Просмотр бронирований |
| `AdminHotelReviews` | Просмотр отзывов |

### Admin System Graph

| Экран | Описание |
|-------|----------|
| `AdminSystemDashboard` | Панель системного администратора |
| `AdminUsersList` | Список пользователей |
| `AdminPendingRequests` | Заявки на администратора |

---

## Модели данных

### Основные модели

```kotlin
// Пользователь
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.USER
)

enum class UserRole { USER, HOTEL_ADMIN, SYSTEM_ADMIN }

// Отель
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

enum class HotelType { HOTEL, RESORT, APARTMENT, HOSTEL, GUEST_HOUSE, VILLA }

// Номер
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

// Отзыв
data class Review(
    val id: Int,
    val hotelId: Int,
    val userId: Int,
    val userName: String,
    val rating: Float,
    val text: String,
    val date: String
)

// Бронирование
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

enum class BookingStatus { PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED }

// Уведомление
data class Notification(
    val id: Int,
    val userId: Int,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val createdAt: String
)

// Параметры поиска
data class SearchParams(
    val city: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val guests: Int = 2,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val hotelTypes: List<HotelType> = emptyList(),
    val minRating: Float? = null,
    val amenities: List<String> = emptyList()
)

// Опции сортировки
enum class SortOption {
    PRICE_ASC,      // Цена: по возрастанию
    PRICE_DESC,     // Цена: по убыванию
    RATING_DESC,    // Рейтинг: по убыванию
    REVIEWS_DESC    // Отзывы: по убыванию
}
```

---

## Сборка и запуск

### Требования

- **Android Studio** (рекомендуется последняя стабильная версия)
- **JDK 11+** (требуется для compileOptions)
- **Android SDK** (API 26–36)

### Команды Gradle

| Команда | Описание |
|---------|----------|
| `.\gradlew assembleDebug` | Сборка debug-APK |
| `.\gradlew assembleRelease` | Сборка release-APK |
| `.\gradlew installDebug` | Установка debug-версии на устройство |
| `.\gradlew test` | Запуск локальных unit-тестов |
| `.\gradlew connectedAndroidTest` | Запуск инструментированных тестов |
| `.\gradlew clean` | Очистка сборки |
| `.\gradlew build` | Полная сборка проекта |

### Минимальная конфигурация

```kotlin
minSdk = 26
targetSdk = 36
compileSdk = 36
```

---

## Зависимости

### Версии (gradle/libs.versions.toml)

```toml
[versions]
agp = "8.13.2"
kotlin = "2.2.0"
coreKtx = "1.10.1"
composeBom = "2024.09.00"
lifecycleRuntimeKtx = "2.6.1"
activityCompose = "1.8.0"
material3 = "1.4.0"
places = "5.1.1"
foundation = "1.10.6"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

### Основные зависимости (app/build.gradle.kts)

```kotlin
// Core Android
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.compose)

// Compose
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.ui.graphics)
implementation(libs.androidx.compose.ui.tooling.preview)
implementation(libs.androidx.compose.material3)

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.6")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

// Coil (загрузка изображений)
implementation("io.coil-kt:coil-compose:2.7.0")
implementation("io.coil-kt:coil-gif:2.7.0")

// Material Icons Extended
implementation("androidx.compose.material:material-icons-extended:1.7.6")

// Kotlinx Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

// Encrypted SharedPreferences
implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

// OkHttp (для Coil)
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Google Places
implementation(libs.places)
implementation(libs.androidx.compose.foundation)
```

---

## Темизация

Приложение использует **синюю/океанскую цветовую схему** согласно спецификации:

### Light Theme

| Токен | Значение | Описание |
|-------|----------|----------|
| `Primary` | `#1E88E5` | Синий морской |
| `OnPrimary` | `#FFFFFF` | Белый |
| `PrimaryContainer` | `#BBDEFB` | Светло-голубой |
| `Secondary` | `#42A5F5` | Светло-голубой |
| `Tertiary` | `#26A69A` | Бирюзовый акцент |
| `Background` | `#FFFFFF` | Белый фон |
| `OnBackground` | `#000000` | Чёрный текст |
| `Surface` | `#FFFFFF` | Белый |

### Dark Theme

| Токен | Значение |
|-------|----------|
| `Primary` | `#64B5F6` |
| `Background` | `#121212` |
| `Surface` | `#1E1E1E` |
| `OnSurface` | `#E0E0E0` |

### Использование темы

```kotlin
@Composable
fun TestAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Отключено
    content: @Composable () -> Unit
)
```

---

## Навигация

Навигация реализована через `NavHost` с sealed class `Screen`:

```kotlin
sealed class Screen(val route: String) {
    // Auth Graph
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Main Graph
    object Home : Screen("home")
    object Search : Screen("search")
    object SearchResults : Screen("search_results")
    object HotelDetails : Screen("hotel_details/{hotelId}") {
        fun createRoute(hotelId: Int) = "hotel_details/$hotelId"
    }
    // ... другие экраны
    
    companion object {
        fun getStartDestination(role: UserRole?): String {
            return when (role) {
                UserRole.HOTEL_ADMIN -> AdminHotelDashboard.route
                UserRole.SYSTEM_ADMIN -> AdminSystemDashboard.route
                UserRole.USER -> Home.route
                null -> Splash.route
            }
        }
    }
}
```

### Графы навигации

```kotlin
sealed class NavGraph(val startDestination: String) {
    object Auth : NavGraph("auth")
    object Main : NavGraph(Screen.Home.route)
    object AdminHotel : NavGraph(Screen.AdminHotelDashboard.route)
    object AdminSystem : NavGraph(Screen.AdminSystemDashboard.route)
}
```

---

## ViewModel и State Management

### AuthViewModel

```kotlin
class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    val currentUser: StateFlow<User?> = repository.currentUser
    
    fun login(): Result<Unit> { ... }
    fun register(): Result<Unit> { ... }
    fun logout() { ... }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: User? = null
)
```

### HotelsViewModel

```kotlin
class HotelsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HotelsUiState())
    val uiState: StateFlow<HotelsUiState> = _uiState.asStateFlow()
    
    fun selectCity(city: String?) { ... }
    fun loadHotels() { ... }
}
```

### SearchViewModel

```kotlin
class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    fun selectCity(city: String?) { ... }
    fun setCheckInDate(date: String) { ... }
    fun setCheckOutDate(date: String) { ... }
    fun setGuests(count: Int) { ... }
    fun setPriceRange(min: Int, max: Int) { ... }
    fun toggleHotelType(type: HotelType) { ... }
    fun toggleAmenity(amenity: String) { ... }
    fun setMinRating(rating: Float) { ... }
    fun setSortOption(option: SortOption) { ... }
    fun performSearch() { ... }
    fun resetFilters() { ... }
}
```

---

## Repository Pattern

**AppRepository** предоставляет единый интерфейс для работы с данными:

```kotlin
class AppRepository {
    // Mock data
    private val users = mutableListOf(...)
    private val hotelsList = listOf(...)
    private val roomsList = mutableListOf(...)
    
    // Auth
    fun login(email: String, password: String): Result<User> { ... }
    fun register(name: String, email: String, password: String): Result<User> { ... }
    fun logout() { ... }
    
    // Hotels
    fun getHotels(): List<Hotel>
    fun searchHotels(params: SearchParams): List<Hotel>
    fun searchHotelsWithSort(params: SearchParams, sortOption: SortOption): List<Hotel>
    fun getAllAmenities(): List<String>
    fun getPriceRange(): ClosedFloatingPointRange<Double>
    fun getAllCities(): List<String>
    
    // Rooms
    fun getRoomsByHotel(hotelId: Int): List<Room>
    
    // Reviews
    fun getReviewsByHotel(hotelId: Int): List<Review>
    fun addReview(...): Result<Review>
    
    // Bookings
    fun createBooking(...): Result<Booking>
    fun getBookingsByUser(userId: Int): List<Booking>
    fun cancelBooking(bookingId: Int): Result<Unit>
    
    // Admin
    fun getAllUsers(): List<User>
    fun updateUserRole(userId: Int, newRole: UserRole): Result<Unit>
}
```

---

## Mock Data

В приложении предустановлены тестовые данные:

### Пользователи для входа

| Email | Пароль | Роль |
|-------|--------|------|
| `user@example.com` | `123456` | USER |
| `hotel@admin.com` | `123456` | HOTEL_ADMIN |
| `system@admin.com` | `123456` | SYSTEM_ADMIN |

### Отели

- **Grand Palace Hotel** (Москва) — 5★, от 15000₽
- **Seaside Resort** (Сочи) — курорт, от 12000₽
- **Mountain Lodge** (Домбай) — гостевой дом, от 9500₽
- **City Business Hotel** (Санкт-Петербург) — бизнес, от 7000₽
- **Historic Boutique Hotel** (Казань) — бутик, от 11000₽
- **Lake View Resort** (Алтай) — эко, от 13500₽

### Города

Москва, Санкт-Петербург, Сочи, Казань, Алтай, Домбай, Калининград

---

## Backend API (в разработке)

Приложение проектируется с учётом будущего backend API. Документация API находится в файле `API_ROUTES.md`.

### Основные эндпоинты

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | `/register` | Регистрация пользователя |
| POST | `/login` | Авторизация |
| POST | `/logout` | Выход из системы |
| GET | `/me` | Получить текущего пользователя |
| GET | `/hotels` | Получить все отели |
| GET | `/hotels/{id}` | Получить отель по ID |
| GET | `/hotels/city/{city}` | Получить отели по городу |
| GET | `/rooms/hotel/{hotelId}` | Получить номера отеля |
| POST | `/bookings` | Создать бронирование |
| GET | `/bookings/user/{userId}` | Получить бронирования пользователя |
| GET | `/reviews/hotel/{hotelId}` | Получить отзывы об отеле |
| POST | `/reviews` | Создать отзыв |

### Base URL

```
http://localhost:8080
```

---

## Разработка

### Код-стайл

В проекте используется **официальный стиль Kotlin**:
```properties
kotlin.code.style=official
```

### JVM-настройки

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

### ProGuard

Для release-сборки минификация отключена:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = false
    }
}
```

---

## Тесты

### Unit-тесты
Расположены в `app/src/test/`. Используют JUnit 4.

### Instrumented-тесты
Расположены в `app/src/androidTest/`. Используют:
- `androidx.test.ext.junit`
- `androidx.espresso.core`
- `androidx.compose.ui.test.junit4`

---

## Этапы разработки (из project.md)

- [x] **Этап 1** — Настройка проекта, архитектура, навигация
- [x] **Этап 2** — Авторизация и регистрация
- [x] **Этап 3** — Главный экран + карточки отелей
- [x] **Этап 4** — Экран отеля
- [x] **Этап 5** — Поиск и фильтры
- [ ] **Этап 6** — Бронирование
- [ ] **Этап 7** — Профиль и история
- [ ] **Этап 8** — Отзывы
- [ ] **Этап 9** — Админ панели
- [ ] **Этап 10** — Полировка UI

---

## Безопасность

- **Encrypted SharedPreferences** — для хранения сессии пользователя
- **usesCleartextTraffic = true** — временно для разработки (будет отключено в production)
- **Пароли** — хранятся в plaintext (mock data, будет хеширование в backend)

---

## Будущие улучшения

- [ ] Завершить экраны бронирования и профиля
- [ ] Интеграция с backend API (Retrofit/Ktor client)
- [ ] Кэширование данных (Room Database)
- [ ] Реальная аутентификация (JWT tokens)
- [ ] Поиск и фильтрация отелей (полная реализация с backend)
- [ ] Избранные отели
- [ ] Карта с расположением отелей
- [ ] Push-уведомления
- [ ] Онлайн-чат с поддержкой
- [ ] Многоязычность
- [ ] Включить ProGuard для release
- [ ] Отключить usesCleartextTraffic
- [ ] Добавить хеширование паролей

---

## Полезные ссылки

- [project.md](project.md) — Полная спецификация приложения
- [USER_FLOW.md](USER_FLOW.md) — Пользовательские сценарии
- [SEARCH_FEATURE.md](SEARCH_FEATURE.md) — Документация системы поиска
- [API_ROUTES.md](API_ROUTES.md) — Документация backend API

---

## Примечания

- Все данные пока локальные (mock data)
- Серверная часть будет добавлена позже
- Архитектура позволяет легко подключить API через Repository
- Приложение поддерживает автоматическое переключение светлой/тёмной темы
- Package name: `com.example.testapp` (наследуется из TestApp)
