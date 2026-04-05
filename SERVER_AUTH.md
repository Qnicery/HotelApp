# Серверная авторизация — Руководство по внедрению

Этот документ описывает процесс внедрения серверной авторизации в приложение HotelApp.

---

## Обзор архитектуры

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                             │
│  ┌──────────────┐              ┌──────────────────┐    │
│  │ LoginScreen  │              │ RegisterScreen   │    │
│  │ - Loading    │              │ - Loading        │    │
│  │ - Error      │              │ - Error          │    │
│  └──────────────┘              └──────────────────┘    │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                 ViewModel Layer                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │              AuthViewModel.kt                    │   │
│  │  - login() -> suspend function                   │   │
│  │  - register() -> suspend function                │   │
│  │  - logout() -> suspend function                  │   │
│  │  - checkAuthStatus() -> валидация токена         │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                 Repository Layer                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │           AuthApiRepository.kt                   │   │
│  │  - login(email, password)                        │   │
│  │  - register(name, email, password)               │   │
│  │  - logout()                                      │   │
│  │  - fetchCurrentUser()                            │   │
│  │  - EncryptedSharedPreferences для токена         │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    API Layer                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │           AuthApiService.kt                      │   │
│  │  @POST("login")                                  │   │
│  │  @POST("register")                               │   │
│  │  @POST("logout")                                 │   │
│  │  @GET("me")                                      │   │
│  └──────────────────────────────────────────────────┘   │
│                            │                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │           RetrofitClient.kt                      │   │
│  │  Base URL: http://10.0.2.2:8080/                 │   │
│  │  OkHttp с логированием                           │   │
│  │  Kotlinx Serialization converter                 │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│              Backend API (Ktor)                         │
│  http://localhost:8080                                  │
│  - POST /login                                          │
│  - POST /register                                       │
│  - POST /logout                                         │
│  - GET /me                                              │
└─────────────────────────────────────────────────────────┘
```

---

## Созданные файлы

### 1. API модели данных
**Файл:** `app/src/main/java/com/example/testapp/data/api/model/ApiAuthModels.kt`

```kotlin
@Serializable
data class UserCreateRequest(val email: String, val name: String, val password: String)

@Serializable
data class UserLoginRequest(val email: String, val password: String)

@Serializable
data class UserLoginResponse(val token: String, val user: UserResponse)

@Serializable
data class UserResponse(val id: Int, val email: String, val name: String, val role: String)
```

### 2. API интерфейс
**Файл:** `app/src/main/java/com/example/testapp/data/api/AuthApiService.kt`

Основные эндпоинты:
- `POST /register` — Регистрация
- `POST /login` — Авторизация
- `POST /logout` — Выход
- `GET /me` — Получить текущего пользователя

### 3. Retrofit клиент
**Файл:** `app/src/main/java/com/example/testapp/data/api/RetrofitClient.kt`

Конфигурация:
- Base URL: `http://10.0.2.2:8080/` (для эмулятора)
- OkHttp с логированием (уровень BODY)
- Kotlinx Serialization converter
- Таймауты: 30 секунд

### 4. API Repository
**Файл:** `app/src/main/java/com/example/testapp/data/repository/AuthApiRepository.kt`

Функциональность:
- Авторизация через сервер
- Регистрация через сервер
- Сохранение токена в EncryptedSharedPreferences
- Валидация токена при запуске
- Безопасный выход из системы

### 5. ViewModel
**Файл:** `app/src/main/java/com/example/testapp/ui/viewmodel/AuthViewModel.kt`

Изменения:
- Использует `AuthApiRepository` вместо mock данных
- Асинхронные функции с `viewModelScope.launch`
- Индикатор загрузки в UI state
- Автоматический логин после регистрации
- Проверка валидности токена при запуске

---

## Зависимости

В `app/build.gradle.kts` добавлены:

```kotlin
// Retrofit для HTTP запросов
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

---

## Безопасность

### EncryptedSharedPreferences

Токен и данные пользователя хранятся в зашифрованном виде:

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

EncryptedSharedPreferences.create(
    context,
    "auth_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### Хранение токена

Токен сохраняется с префиксом `Bearer `:
```kotlin
saveToken(token) // Сохраняет "Bearer <token>"
```

Использование в запросах:
```kotlin
@Header("Authorization") token: String
// Передаётся "Bearer <token>"
```

---

## Поток авторизации

### Вход в систему

```
1. Пользователь вводит email и пароль
2. Валидация на клиенте (пустые поля)
3. ViewModel.login() -> AuthApiRepository.login()
4. POST запрос на /login
5. Получаем UserLoginResponse с токеном
6. Сохраняем токен в EncryptedSharedPreferences
7. Преобразуем UserResponse -> User
8. Обновляем StateFlow<User?>
9. UI получает обновление и переходит на главный экран
```

### Регистрация

```
1. Пользователь вводит имя, email, пароль
2. Валидация на клиенте (пустые поля, мин. 8 символов, совпадение паролей)
3. ViewModel.register() -> AuthApiRepository.register()
4. POST запрос на /register
5. Получаем UserResponse (без токена)
6. Автоматический вызов login() для получения токена
7. Сохраняем токен и данные пользователя
8. Обновляем StateFlow<User?>
9. UI получает обновление и переходит на главный экран
```

### Проверка авторизации при запуске

```
1. При запуске проверяем наличие токена в SharedPreferences
2. Если токен есть -> fetchCurrentUser()
3. GET запрос на /me с токеном
4. Если успех -> пользователь авторизован
5. Если ошибка (токен невалиден) -> logout()
```

### Выход из системы

```
1. ViewModel.logout() -> AuthApiRepository.logout()
2. POST запрос на /logout с токеном
3. Очищаем EncryptedSharedPreferences
4. Обновляем StateFlow<User?> = null
5. UI получает обновление и переходит на экран входа
```

---

## Обработка ошибок

### Коды ответов сервера

| Код | Значение | Действие |
|-----|----------|----------|
| 200 | OK | Успешная авторизация/выход |
| 201 | Created | Успешная регистрация |
| 400 | Bad Request | Неверный формат данных |
| 401 | Unauthorized | Неверные учётные данные |
| 500 | Server Error | Ошибка сервера |

### Сообщения об ошибках

```kotlin
when (response.code()) {
    400 -> "Неверный формат данных"
    401 -> "Неверный email или пароль"
    else -> "Ошибка сервера: ${response.code()} - ${response.errorBody()?.string()}"
}
```

### Ошибки сети

```kotlin
catch (e: Exception) {
    Result.failure(Exception("Ошибка сети: ${e.message}"))
}
```

---

## Тестирование

### Требования

1. **Backend сервер** должен быть запущен на `localhost:8080`
2. Для эмулятора Android используется `10.0.2.2` вместо `localhost`
3. Для физического устройства используйте IP компьютера

### Тестовые учётные записи

Создайте их на сервере или используйте API регистрации:

```kotlin
// Регистрация нового пользователя
POST /register
{
  "email": "test@example.com",
  "name": "Тест Пользователь",
  "password": "testpass123"
}

// Авторизация
POST /login
{
  "email": "test@example.com",
  "password": "testpass123"
}
```

### Логирование запросов

OkHttp настроен с уровнем логирования `BODY`:

```
D/OkHttp: --> POST http://10.0.2.2:8080/login
D/OkHttp: Content-Type: application/json
D/OkHttp: {"email":"test@example.com","password":"testpass123"}
D/OkHttp: --> END POST
D/OkHttp: <-- 200 OK http://10.0.2.2:8080/login
D/OkHttp: {"token":"abc123","user":{"id":1,"email":"test@example.com",...}}
D/OkHttp: <-- END HTTP
```

---

## Настройка для production

### 1. Изменение Base URL

```kotlin
// RetrofitClient.kt
private const val BASE_URL = if (BuildConfig.DEBUG) {
    "http://10.0.2.2:8080/"  // Эмулятор
} else {
    "https://api.hotelapp.com/"  // Production
}
```

### 2. Отключение логирования в production

```kotlin
if (BuildConfig.DEBUG) {
    okHttpClient.addInterceptor(loggingInterceptor)
}
```

### 3. Certificate Pinning (опционально)

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.hotelapp.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

---

## Интеграция с другими экранами

### ProfileScreen

```kotlin
val authViewModel: AuthViewModel = viewModel()
val currentUser by authViewModel.currentUser.collectAsState()

Text("Привет, ${currentUser?.name}!")
```

### Navigation

```kotlin
// В MainActivity или AppNavigation
val currentUser by authViewModel.currentUser.collectAsState()

val startDestination = if (currentUser != null) {
    Screen.Home.route
} else {
    Screen.Splash.route
}
```

---

## Следующие шаги

### 1. Реализовать остальные API эндпоинты

- [ ] GET /hotels — Получить все отели
- [ ] GET /hotels/{id} — Получить отель
- [ ] GET /rooms/hotel/{hotelId} — Получить номера
- [ ] POST /bookings — Создать бронирование
- [ ] GET /bookings/user/{userId} — Получить бронирования
- [ ] POST /reviews — Создать отзыв

### 2. Добавить Interceptor для автоматической авторизации

```kotlin
class AuthInterceptor(private val authApiRepository: AuthApiRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authApiRepository.getAuthToken()
        val request = chain.request().newBuilder()
            .addHeader("Authorization", token ?: "")
            .build()
        return chain.proceed(request)
    }
}
```

### 3. Обработка 401 ошибки (истёкший токен)

```kotlin
class AuthErrorInterceptor(private val authViewModel: AuthViewModel) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            authViewModel.logout()
        }
        return response
    }
}
```

### 4. Refresh token логика

Если сервер поддерживает refresh tokens:

```kotlin
class TokenAuthenticator(
    private val authApiRepository: AuthApiRepository
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = authApiRepository.getRefreshToken()
        val newToken = authApiRepository.refreshAccessToken(refreshToken)
        
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }
}
```

---

## Troubleshooting

### Ошибка: "Connection refused"

**Причина:** Сервер не запущен или неправильный URL

**Решение:**
1. Проверьте, что сервер запущен: `curl http://localhost:8080`
2. Для эмулятора используйте `10.0.2.2` вместо `localhost`
3. Проверьте firewall

### Ошибка: "SSL handshake failed"

**Причина:** Проблемы с HTTPS сертификатом (development)

**Решение:**
1. Временно используйте HTTP для разработки
2. Добавьте network security config для игнорирования ошибок SSL
3. В production используйте валидный сертификат

### Ошибка: "Unexpected token"

**Причина:** Сервер возвращает невалидный JSON

**Решение:**
1. Проверьте логи сервера
2. Включите логирование OkHttp
3. Проверьте Content-Type заголовок

---

## Полезные ссылки

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [OkHttp Documentation](https://square.github.io/okhttp/)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
- [API Routes Documentation](API_ROUTES.md)
