package com.example.testapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.testapp.data.api.RetrofitClient
import com.example.testapp.data.api.model.UserCreateRequest
import com.example.testapp.data.api.model.UserLoginRequest
import com.example.testapp.data.model.User
import com.example.testapp.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Репозиторий для работы с API авторизации
 * Использует Retrofit для запросов к серверу
 * Сохраняет токен и данные пользователя в EncryptedSharedPreferences
 */
class AuthApiRepository(private val context: Context) {

    companion object {
        @Volatile
        private var instance: AuthApiRepository? = null

        fun getInstance(context: Context): AuthApiRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthApiRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== SharedPreferences для хранения токена и пользователя ====================

    private val sharedPreferences: SharedPreferences by lazy {
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
    }

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", "Bearer $token").apply()
    }

    private fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    private fun saveUserToPrefs(user: User) {
        sharedPreferences.edit().apply {
            putInt("user_id", user.id)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_role", user.role.name)
            user.avatarUrl?.let { putString("user_avatar", it) }
            apply()
        }
    }

    private fun loadUserFromPrefs(): User? {
        return try {
            val id = sharedPreferences.getInt("user_id", -1)
            if (id == -1) return null

            val name = sharedPreferences.getString("user_name", "") ?: ""
            val email = sharedPreferences.getString("user_email", "") ?: ""
            val role = sharedPreferences.getString("user_role", "USER") ?: "USER"
            val avatar = sharedPreferences.getString("user_avatar", null)

            User(
                id = id,
                name = name,
                email = email,
                password = "",
                avatarUrl = avatar,
                role = UserRole.valueOf(role)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun clearAuthPrefs() {
        sharedPreferences.edit().clear().apply()
    }

    // ==================== State ====================

    private val _currentUser = MutableStateFlow<User?>(loadUserFromPrefs())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ==================== API вызовы ====================

    /**
     * Авторизация пользователя
     * POST /login
     * 
     * Сервер возвращает только токен, поэтому после получения токена
     * делаем дополнительный запрос GET /me для получения данных пользователя
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // ЛОГИРОВАНИЕ: Выводим данные запроса (для отладки)
            android.util.Log.d("AuthApiRepository", "=== LOGIN REQUEST ===")
            android.util.Log.d("AuthApiRepository", "Email: $email")
            android.util.Log.d("AuthApiRepository", "Password length: ${password.length}")

            val loginResponse = RetrofitClient.authApi.login(UserLoginRequest(email, password))

            // ЛОГИРОВАНИЕ: Выводим ответ сервера
            android.util.Log.d("AuthApiRepository", "=== LOGIN RESPONSE ===")
            android.util.Log.d("AuthApiRepository", "Response code: ${loginResponse.code()}")
            android.util.Log.d("AuthApiRepository", "Response message: ${loginResponse.message()}")

            if (loginResponse.isSuccessful) {
                val loginBody = loginResponse.body()
                android.util.Log.d("AuthApiRepository", "Response body: $loginBody")

                if (loginBody != null && loginBody.token.isNotEmpty()) {
                    // Сохраняем токен
                    saveToken(loginBody.token)
                    android.util.Log.d("AuthApiRepository", "Token saved: ${loginBody.token.take(20)}...")

                    // Получаем данные пользователя через GET /me
                    android.util.Log.d("AuthApiRepository", "Fetching user data via GET /me...")
                    val userResult = fetchCurrentUser()
                    
                    if (userResult.isSuccess) {
                        val user = userResult.getOrNull()!!
                        android.util.Log.d("AuthApiRepository", "User fetched: ${user.name}, role: ${user.role}")
                        Result.success(user)
                    } else {
                        val error = userResult.exceptionOrNull()?.message ?: "Не удалось получить данные пользователя"
                        android.util.Log.e("AuthApiRepository", error)
                        // Очищаем токен, так как не удалось получить пользователя
                        clearAuthPrefs()
                        Result.failure(Exception(error))
                    }
                } else {
                    android.util.Log.e("AuthApiRepository", "Login response body is null or token is empty")
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                val errorBody = loginResponse.errorBody()?.string()
                android.util.Log.e("AuthApiRepository", "Error response: ${loginResponse.code()} - $errorBody")

                val errorMessage = when (loginResponse.code()) {
                    400 -> "Неверный формат данных"
                    401 -> "Неверный email или пароль (401 Unauthorized)"
                    else -> "Ошибка сервера: ${loginResponse.code()} - $errorBody"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthApiRepository", "Network error: ${e.message}", e)
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Регистрация пользователя
     * POST /register
     */
    suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            // ЛОГИРОВАНИЕ
            android.util.Log.d("AuthApiRepository", "=== REGISTER REQUEST ===")
            android.util.Log.d("AuthApiRepository", "Name: $name")
            android.util.Log.d("AuthApiRepository", "Email: $email")
            android.util.Log.d("AuthApiRepository", "Password length: ${password.length}")

            val response = RetrofitClient.authApi.register(UserCreateRequest(email, name, password))

            // ЛОГИРОВАНИЕ
            android.util.Log.d("AuthApiRepository", "=== REGISTER RESPONSE ===")
            android.util.Log.d("AuthApiRepository", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val userResponse = response.body()
                android.util.Log.d("AuthApiRepository", "Response body: $userResponse")

                if (userResponse != null) {
                    val user = User(
                        id = userResponse.id,
                        name = userResponse.name,
                        email = userResponse.email,
                        password = "",
                        role = parseUserRole(userResponse.role)
                    )

                    android.util.Log.d("AuthApiRepository", "User registered: ${user.name}, id: ${user.id}")

                    Result.success(user)
                } else {
                    android.util.Log.e("AuthApiRepository", "Register response body is null")
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthApiRepository", "Register error: ${response.code()} - $errorBody")

                val errorMessage = when (response.code()) {
                    400 -> "Неверный формат данных или email уже занят"
                    else -> "Ошибка сервера: ${response.code()} - $errorBody"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthApiRepository", "Register network error: ${e.message}", e)
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Выход из системы
     * POST /logout
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val token = getToken()
            if (token.isNullOrEmpty()) {
                // Если токена нет, просто очищаем данные
                clearAuthPrefs()
                _currentUser.value = null
                return Result.success(Unit)
            }

            val response = RetrofitClient.authApi.logout(token)

            // Очищаем данные независимо от результата
            clearAuthPrefs()
            _currentUser.value = null

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Даже если сервер вернул ошибку, локально вышли
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Даже если ошибка сети, локально вышли
            clearAuthPrefs()
            _currentUser.value = null
            Result.success(Unit)
        }
    }

    /**
     * Получить текущего пользователя с сервера
     * GET /me
     */
    suspend fun fetchCurrentUser(): Result<User> {
        return try {
            val token = getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Токен не найден"))
            }

            val response = RetrofitClient.authApi.getCurrentUser(token)

            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    val user = User(
                        id = userResponse.id,
                        name = userResponse.name,
                        email = userResponse.email,
                        password = "",
                        role = parseUserRole(userResponse.role)
                    )

                    saveUserToPrefs(user)
                    _currentUser.value = user

                    Result.success(user)
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
     * Получить сохранённого пользователя (без запроса к серверу)
     */
    fun getCurrentUser(): User? = _currentUser.value

    /**
     * Получить токен для авторизации
     */
    fun getAuthToken(): String? = getToken()

    /**
     * Проверка авторизации
     */
    fun isAuthorized(): Boolean = getToken() != null && _currentUser.value != null

    /**
     * Парсинг роли из строки сервера
     * Сервер может возвращать: "User", "Admin", "Moderator", "USER", "ADMIN" и т.д.
     */
    private fun parseUserRole(role: String): UserRole {
        return when (role.lowercase()) {
            "admin", "hotel_admin", "moderator" -> UserRole.HOTEL_ADMIN
            "system_admin", "systemadmin" -> UserRole.SYSTEM_ADMIN
            else -> UserRole.USER
        }
    }
}
