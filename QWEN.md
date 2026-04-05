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
- **Repository Pattern** — абстракция доступа к данным
- **Retrofit + Kotlinx Serialization** — работа с backend API
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
│   │   │   │   ├── api/                           # API слой
│   │   │   │   │   ├── model/                     # API модели (DTO)
│   │   │   │   │   │   ├── ApiAuthModels.kt       # Модели авторизации
│   │   │   │   │   │   ├── ApiBookingModels.kt    # Модели бронирований
│   │   │   │   │   │   ├── ApiHotelModels.kt      # Модели отелей/комнат
│   │   │   │   │   │   └── ApiReviewModels.kt     # Модели отзывов
│   │   │   │   │   ├── AuthApiService.kt          # API авторизации
│   │   │   │   │   ├── BookingApiService.kt       # API бронирований
│   │   │   │   │   ├── HotelsApiService.kt        # API отелей
│   │   │   │   │   ├── ReviewsApiService.kt       # API отзывов
│   │   │   │   │   ├── RetrofitClient.kt          # Retrofit клиент (синглтон)
│   │   │   │   │   └── ServerConfig.kt            # Конфигурация сервера (BASE_URL)
│   │   │   │   ├── model/
│   │   │   │   │   └── Models.kt                  # Модели приложения
│   │   │   │   └── repository/
│   │   │   │       ├── AppRepository.kt           # Репозиторий (mock данные для бронирований)
│   │   │   │       ├── AuthApiRepository.kt       # Репозиторий авторизации (API)
│   │   │   │       ├── HotelsRepository.kt        # Репозиторий отелей (API)
│   │   │   │       ├── ProfileRepository.kt       # Репозиторий профиля (API)
│   │   │   │       └── ReviewsRepository.kt       # Репозиторий отзывов (API)
│   │   │   ├── ui/
│   │   │   │   ├── components/                    # Переиспользуемые UI-компоненты
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── Screen.kt                  # sealed class навигации
│   │   │   │   │   └── AppNavigation.kt           # Граф навигации
│   │   │   │   ├── screens/
│   │   │   │   │   ├── SplashScreen.kt            # Экран приветствия
│   │   │   │   │   ├── LoginScreen.kt             # Экран авторизации
│   │   │   │   │   ├── RegisterScreen.kt          # Экран регистрации
│   │   │   │   │   ├── HomeScreen.kt              # Главный экран со списком отелей
│   │   │   │   │   ├── SearchScreen.kt            # Экран поиска
│   │   │   │   │   ├── SearchResultsScreen.kt     # Результаты поиска
│   │   │   │   │   ├── HotelDetailsScreen.kt      # Детали отеля
│   │   │   │   │   ├── RoomsListScreen.kt         # Список номеров
│   │   │   │   │   ├── BookingScreen.kt           # Бронирование
│   │   │   │   │   ├── ProfileScreen.kt           # Профиль пользователя
│   │   │   │   │   ├── BookingHistoryScreen.kt    # История бронирований
│   │   │   │   │   ├── ReviewsScreen.kt           # Отзывы
│   │   │   │   │   ├── NotificationsScreen.kt     # Уведомления (заглушка)
│   │   │   │   │   ├── SettingsScreen.kt          # Настройки
│   │   │   │   │   └── admin/                     # Экраны администратора
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt                   # Цветовая палитра
│   │   │   │   │   ├── Theme.kt                   # Тема приложения
│   │   │   │   │   └── Type.kt                    # Типографика
│   │   │   │   └── viewmodel/
│   │   │   │       ├── AuthViewModel.kt           # ViewModel авторизации
│   │   │   │       ├── HotelsViewModel.kt         # ViewModel списка отелей
│   │   │   │       ├── HotelDetailsViewModel.kt   # ViewModel деталей отеля
│   │   │   │       ├── BookingViewModel.kt
│   │   │   │       ├── ProfileViewModel.kt        # ViewModel профиля
│   │   │   │       ├── ReviewsViewModel.kt        # ViewModel отзывов
│   │   │   │       ├── SearchViewModel.kt         # ViewModel поиска
│   │   │   │       └── AdminViewModel.kt
│   │   │   └── MainActivity.kt                    # Главная Activity
│   │   ├── res/                                   # Ресурсы
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml                         # Version catalog зависимостей
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── project.md                                     # Полная спецификация
├── USER_FLOW.md                                   # Пользовательские сценарии
├── SEARCH_FEATURE.md                              # Документация поиска
├── API_ROUTES.md                                  # Документация backend API
├── SERVER_AUTH.md                                 # Документация авторизации
├── LOG.md                                         # Лог изменений
├── TODO.md                                        # Список задач
└── QWEN.md                                        # Этот файл
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
│  ┌────────────────┐  ┌───────────────┐  ┌────────────┐ │
│  │AuthApiRepository│  │HotelsRepository│ │ProfileRepo │ │
│  │  (API /me)     │  │  (API hotels) │  │ (API prof) │ │
│  └────────────────┘  └───────────────┘  └────────────┘ │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                  API Layer (Retrofit)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐ │
│  │  AuthApi     │  │ HotelsApi    │  │ ReviewsApi    │ │
│  │  Service     │  │  Service     │  │  Service      │ │
│  └──────────────┘  └──────────────┘  └───────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## Backend API

### Базовый URL
```
http://10.0.2.2:8080/
```

**ServerConfig.kt** содержит единую точку конфигурации:
```kotlin
object ServerConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"
    
    fun getImageUrl(imagePath: String?): String
    fun getImageUrls(imagePaths: List<String>?): List<String>
}
```

### Реализованные API эндпоинты

| Метод | Эндпоинт | Описание | Репозиторий |
|-------|----------|----------|-------------|
| POST | `/register` | Регистрация | AuthApiRepository |
| POST | `/login` | Авторизация | AuthApiRepository |
| POST | `/logout` | Выход | AuthApiRepository |
| GET | `/me` | Текущий пользователь | AuthApiRepository |
| GET | `/hotels` | Все отели | HotelsRepository |
| GET | `/hotels/{id}` | Отель по ID | HotelsRepository |
| GET | `/hotels/city/{city}` | Отели по городу | HotelsRepository |
| GET | `/rooms/hotel/{hotelId}` | Комнаты отеля | HotelsRepository |
| GET | `/bookings/user/{userId}` | Бронирования пользователя | ProfileRepository |
| PUT | `/bookings/{id}/status` | Обновить статус | ProfileRepository |
| GET | `/reviews/hotel/{hotelId}` | Отзывы отеля | ReviewsRepository |
| POST | `/reviews` | Создать отзыв | ReviewsRepository |

### Неполная интеграция (требуется доработка сервера)

- ❌ **Уведомления** — нет эндпоинтов в API_ROUTES.md
- ❌ **Создание отзыва** — требует `bookingId`, который недоступен клиенту
- ⚠️ **HotelDTO** — не возвращает `photoUrls`, `type`, `reviewsCount`, `amenities`
- ⚠️ **RoomDTO** — не возвращает `photoUrls`
- ⚠️ **BookingDTO** — не возвращает `hotelId`, `hotelName`, `roomName`, `guests`, `totalPrice`

Полная документация API: **API_ROUTES.md**

---

## Роли пользователей

| Роль | Описание | Стартовый экран |
|------|----------|-----------------|
| `USER` | Обычный пользователь | Home (список отелей) |
| `HOTEL_ADMIN` | Администратор отеля | AdminHotelDashboard |
| `SYSTEM_ADMIN` | Системный администратор | AdminSystemDashboard |

---

## Экраны приложения

### Auth Graph
- `Splash` — приветственный экран
- `Login` — авторизация
- `Register` — регистрация

### Main Graph
- `Home` — список отелей с фильтрами по городам
- `Search` — расширенный поиск
- `SearchResults` — результаты поиска
- `HotelDetails` — детали отеля
- `RoomsList` — список номеров
- `Booking` — оформление бронирования
- `Profile` — профиль пользователя (данные с сервера)
- `BookingHistory` — история бронирований
- `Reviews` — отзывы об отеле
- `Notifications` — уведомления (заглушка)
- `Settings` — настройки

### Admin Graphs
- `AdminHotelDashboard` — панель администратора отеля
- `AdminSystemDashboard` — панель системного администратора

---

## Тестовые аккаунты

| Email | Пароль | Роль |
|-------|--------|------|
| `user@example.com` | `123456` | USER |
| `hotel@admin.com` | `123456` | HOTEL_ADMIN |
| `system@admin.com` | `123456` | SYSTEM_ADMIN |

---

## Сборка и запуск

### Требования
- **Android Studio** (последняя стабильная версия)
- **JDK 11+**
- **Android SDK** (API 26–36)

### Команды Gradle

```bash
# Сборка debug-APK
.\gradlew.bat assembleDebug

# Установка на устройство/эмулятор
.\gradlew.bat installDebug

# Запуск приложения
adb shell am start -n com.example.testapp/.MainActivity

# Полная сборка
.\gradlew.bat build

# Очистка
.\gradlew.bat clean

# Запуск тестов
.\gradlew.bat test
```

### Минимальная конфигурация
```kotlin
minSdk = 26
targetSdk = 36
compileSdk = 36
```

---

## Ключевые зависимости

| Библиотека | Версия | Назначение |
|-----------|--------|------------|
| Kotlin | 2.2.0 | Язык разработки |
| AGP | 8.13.2 | Android Gradle Plugin |
| Compose BOM | 2024.09.00 | UI-фреймворк |
| Navigation Compose | 2.8.6 | Навигация |
| ViewModel Compose | 2.8.7 | MVVM |
| Coil | 2.7.0 | Загрузка изображений |
| Retrofit | (через libs) | HTTP-клиент |
| Kotlinx Serialization | 1.6.3 | JSON |
| Security Crypto | 1.1.0-alpha06 | Encrypted SharedPreferences |
| OkHttp | 4.12.0 | HTTP-клиент для Coil |

---

## Архитектурные принципы

### Repository Pattern
Каждый домен имеет свой репозиторий:
- **AuthApiRepository** — авторизация, сессия, токен
- **HotelsRepository** — отели, комнаты, города
- **ProfileRepository** — профиль пользователя, бронирования
- **ReviewsRepository** — отзывы, статистика

### State Management
Все ViewModel используют `StateFlow`:
```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

### Image Loading
Все URL изображений проходят через `ServerConfig.getImageUrl()`:
```kotlin
val imageUrl = ServerConfig.getImageUrl(dto.photoUrls?.firstOrNull())
```

---

## Известные проблемы и TODO

Полный список задач: **TODO.md**

### 🔴 Высокий приоритет
1. Загрузка удобств комнат через `GET /room-amenities/room/{roomId}`
2. Добавить недостающие поля в BookingDTO (hotelId, hotelName, roomName, guests, totalPrice)
3. Создать bookingId endpoint для отзывов

### 🟡 Средний приоритет
4. Добавить поле `type` в HotelDTO (сейчас определяется из описания)
5. Добавить `reviewsCount` в HotelDTO
6. UI placeholders для изображений

### 🟢 Низкий приоритет
7. Уведомления API
8. Cleanup deprecated icons
9. Pull-to-refresh

---

## Безопасность

- **EncryptedSharedPreferences** — хранение токена и данных пользователя
- **usesCleartextTraffic = true** — временно для разработки (будет отключено в production)
- **Пароли** — хранятся в plaintext на сервере (будет хеширование)

---

## Полезные ссылки

- [project.md](project.md) — Полная спецификация приложения
- [USER_FLOW.md](USER_FLOW.md) — Пользовательские сценарии
- [SEARCH_FEATURE.md](SEARCH_FEATURE.md) — Документация системы поиска
- [API_ROUTES.md](API_ROUTES.md) — Документация backend API
- [SERVER_AUTH.md](SERVER_AUTH.md) — Документация авторизации
- [TODO.md](TODO.md) — Список задач
- [LOG.md](LOG.md) — Лог изменений
