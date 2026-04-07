# HotelApp — Android-приложение для бронирования отелей

## Обзор проекта

**HotelApp** — Android-приложение для поиска, просмотра и бронирования отелей.

- **Kotlin** — основной язык
- **Jetpack Compose** — декларативный UI
- **Material 3** — дизайн-система
- **MVVM + Repository Pattern** — архитектура
- **Navigation Compose** — навигация с графами по ролям
- **Retrofit + Kotlinx Serialization** — REST API
- **Coil** — загрузка изображений
- **EncryptedSharedPreferences** — безопасное хранение сессии

**Package:** `com.example.testapp` | **minSdk:** 26 | **targetSdk:** 36 | **Kotlin:** 2.2.0 | **AGP:** 8.13.2

---

## Структура проекта

```
TestApp/
├── app/src/main/java/com/example/testapp/
│   ├── data/
│   │   ├── api/                          # API слой
│   │   │   ├── model/                    # DTO (ApiAuthModels, ApiBookingModels, ApiHotelModels, ApiReviewModels)
│   │   │   ├── AmenitiesApiService.kt
│   │   │   ├── AuthApiService.kt
│   │   │   ├── BookingApiService.kt
│   │   │   ├── HotelsApiService.kt
│   │   │   ├── RetrofitClient.kt          # Singleton Retrofit
│   │   │   ├── ReviewsApiService.kt
│   │   │   └── ServerConfig.kt            # BASE_URL = "http://10.0.2.2:8080/"
│   │   ├── model/
│   │   │   └── Models.kt                  # Модели приложения (User, Hotel, Room, Booking, Review...)
│   │   └── repository/
│   │       ├── AppRepository.kt           # Legacy: mock-данные (не используется новыми экранами)
│   │       ├── AuthApiRepository.kt       # Авторизация, сессия, токен (API)
│   │       ├── BookingRepository.kt       # Бронирования (API)
│   │       ├── HotelsRepository.kt        # Отели, комнаты, города, удобства (API)
│   │       ├── ProfileRepository.kt       # Профиль, бронирования пользователя (API)
│   │       └── ReviewsRepository.kt       # Отзывы, статистика (API)
│   ├── ui/
│   │   ├── components/                    # Переиспользуемые UI (PickerDialogs, MaterialDateRangePicker)
│   │   ├── navigation/
│   │   │   ├── Screen.kt                  # sealed class экранов
│   │   │   └── AppNavigation.kt           # NavHost с 4 графами
│   │   ├── screens/                       # Все экраны приложения
│   │   │   └── admin/                     # Админ-панели
│   │   ├── theme/                         # Цвета, тема, типографика
│   │   └── viewmodel/                     # Все ViewModel
│   └── MainActivity.kt
├── gradle/libs.versions.toml              # Version Catalog
├── API_ROUTES.md                          # Полная документация backend API
├── TODO.md                                # Список задач
├── LOG.md                                 # Лог изменений
├── project.md                             # Спецификация
├── USER_FLOW.md                           # Пользовательские сценарии
├── SEARCH_FEATURE.md                      # Документация поиска
└── SERVER_AUTH.md                         # Документация авторизации
```

---

## Архитектура

```
┌─────────────────────────────────────────────────┐
│              UI Layer (Compose Screens)          │
│  Splash → Login → Home → Search → Results → ... │
└──────────────────────┬──────────────────────────┘
                       ▼
┌─────────────────────────────────────────────────┐
│           ViewModel Layer (StateFlow)            │
│  AuthVM, HotelsVM, SearchVM, BookingVM, ...     │
└──────────────────────┬──────────────────────────┘
                       ▼
┌─────────────────────────────────────────────────┐
│         Repository Layer (Singleton per ctx)     │
│  AuthApiRepo, HotelsRepo, BookingRepo, ...      │
└──────────────────────┬──────────────────────────┘
                       ▼
┌─────────────────────────────────────────────────┐
│           API Layer (Retrofit + OkHttp)          │
│  AuthApi, HotelsApi, BookingApi, ReviewsApi     │
└─────────────────────────────────────────────────┘
```

### Ключевые принципы

- **Repository Pattern** — каждый домен имеет свой репозиторий с `getInstance(context)` singleton
- **StateFlow** — все ViewModel используют `MutableStateFlow` + `asStateFlow()`
- **ServerConfig** — все URL изображений проходят через `ServerConfig.getImageUrl()`
- **Result<T>** — репозитории возвращают `Result<T>` с обработкой ошибок

---

## Backend API

**Base URL:** `http://10.0.2.2:8080/` (для эмулятора)

| Метод | Эндпоинт | Описание | Репозиторий |
|-------|----------|----------|-------------|
| POST | `/register` | Регистрация | AuthApiRepository |
| POST | `/login` | Авторизация (возвращает `{"token":"..."}`) | AuthApiRepository |
| POST | `/logout` | Выход | AuthApiRepository |
| GET | `/me` | Текущий пользователь | AuthApiRepository |
| GET | `/hotels` | Все отели | HotelsRepository |
| GET | `/hotels/{id}` | Отель по ID | HotelsRepository |
| GET | `/hotels/city/{city}` | Отели по городу | HotelsRepository |
| GET | `/rooms/{id}` | Комната по ID | HotelsApiService |
| GET | `/rooms/hotel/{hotelId}` | Комнаты отеля | HotelsRepository |
| GET | `/amenities` | Все удобства | HotelsRepository |
| GET | `/room-amenities/hotel/{hotelId}` | Удобства отеля | HotelsRepository |
| GET | `/room-amenities/room/{roomId}` | Удобства комнаты | AmenitiesApiService |
| POST | `/bookings` | Создать бронирование | BookingRepository |
| GET | `/bookings/{id}` | Бронирование по ID | BookingRepository |
| GET | `/bookings/user/{userId}` | Бронирования пользователя | BookingRepository |
| PUT | `/bookings/{id}/status` | Обновить статус | BookingRepository |
| GET | `/reviews/hotel/{hotelId}` | Отзывы отеля (с статистикой) | ReviewsRepository |
| POST | `/reviews` | Создать отзыв (требует `bookingId`) | ReviewsRepository |

### Формат дат

Бронирования: ISO-8601 с временем — `"2026-04-16T14:00:00Z"` (заезд 14:00, выезд 12:00)

---

## Навигация

### Графы по ролям

| Роль | Стартовый экран |
|------|-----------------|
| `USER` | Home (список отелей) |
| `HOTEL_ADMIN` | AdminHotelDashboard |
| `SYSTEM_ADMIN` | AdminSystemDashboard |

### Основные экраны (User)

`Splash` → `Login` / `Register` → `Home` → `Search` → `SearchResults` → `HotelDetails` → `RoomsList` → `Booking`

Параметры поиска (city, checkIn, checkOut, guests) передаются через query-параметры навигации и инициализируются в ViewModel каждого экрана.

### Экраны администратора

- **Hotel Admin:** Dashboard → Edit Hotel → Rooms List → Room Edit → Bookings → Reviews
- **System Admin:** Dashboard → Users List → User Edit → Pending Requests

---

## Тестовые аккаунты

| Email | Пароль | Роль |
|-------|--------|------|
| `user@example.com` | `123456` | USER |
| `hotel@admin.com` | `123456` | HOTEL_ADMIN |
| `system@admin.com` | `1233456` | SYSTEM_ADMIN |

---

## Сборка и запуск

### Требования
- **Android Studio** (последняя стабильная)
- **JDK 11+**
- **Android SDK** API 26–36

### Команды

```bash
# Сборка debug-APK
.\gradlew.bat assembleDebug

# Установка на устройство/эмулятор
.\gradlew.bat installDebug

# Полная сборка
.\gradlew.bat build

# Очистка
.\gradlew.bat clean
```

---

## Ключевые зависимости

| Библиотека | Назначение |
|-----------|------------|
| Kotlin 2.2.0 | Язык |
| Compose BOM 2024.09.00 | UI |
| Navigation Compose 2.8.6 | Навигация |
| ViewModel Compose 2.8.7 | MVVM |
| Coil 2.7.0 | Изображения |
| Retrofit 2.11.0 | HTTP |
| Kotlinx Serialization 1.6.3 | JSON |
| Security Crypto 1.1.0-alpha06 | Encrypted SharedPreferences |
| OkHttp 4.12.0 | HTTP-клиент |

---

## Известные ограничения и TODO

Полный список: **TODO.md**

### Текущие задачи
- [ ] **Создание отзыва** — POST `/reviews` требует `bookingId`, недоступный клиенту
- [ ] **Уведомления API** — нет эндпоинтов `/notifications/*`
- [ ] **UI placeholders** для изображений при загрузке
- [ ] **Deprecated icons** — заменить `Icons.Default.ArrowBack` → `Icons.AutoMirrored.Filled.ArrowBack`
- [ ] **Deprecated Locale** — убрать `constructor(p0: String!): Locale`
- [ ] **Pull-to-refresh** на HomeScreen

---

## Безопасность

- **EncryptedSharedPreferences** — хранение токена и данных пользователя
- **usesCleartextTraffic = true** — временно для разработки (будет отключено в production)
- Пароли на сервере хранятся в plaintext (требуется хеширование)
