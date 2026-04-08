package com.example.testapp.data.repository

import android.content.Context
import com.example.testapp.data.api.AdminRequestsApiService
import com.example.testapp.data.api.RetrofitClient
import com.example.testapp.data.api.UsersApiService
import com.example.testapp.data.api.model.AdminRequestCreateRequest
import com.example.testapp.data.api.model.AdminRequestDTO
import com.example.testapp.data.api.model.AdminRequestStatusUpdateRequest
import com.example.testapp.data.api.model.HotelDTO
import com.example.testapp.data.api.model.UserResponse
import com.example.testapp.data.api.model.UserRoleUpdateRequest
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.HotelType
import com.example.testapp.data.model.User
import com.example.testapp.data.model.UserRole

/**
 * Репозиторий для системного администратора
 * Инкапсулирует работу с API: пользователи, заявки на админа, отели, статистика
 */
class AdminSystemRepository private constructor(context: Context) {

    private val usersApi: UsersApiService = RetrofitClient.usersApi
    private val adminRequestsApi: AdminRequestsApiService = RetrofitClient.adminRequestsApi
    private val hotelsRepository = HotelsRepository.getInstance(context)
    private val reviewsRepository = ReviewsRepository.getInstance(context)

    companion object {
        @Volatile
        private var instance: AdminSystemRepository? = null

        fun getInstance(context: Context): AdminSystemRepository {
            return instance ?: synchronized(this) {
                instance ?: AdminSystemRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== Пользователи ====================

    /**
     * Получить всех пользователей
     * GET /users
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val response = usersApi.getAllUsers()

            if (response.isSuccessful) {
                val userResponses = response.body() ?: emptyList()
                val users = userResponses.map { mapUserResponseToUser(it) }
                Result.success(users)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Обновить роль пользователя
     * PUT /users/{id}/role
     */
    suspend fun updateUserRole(userId: Int, role: String): Result<User> {
        return try {
            val request = UserRoleUpdateRequest(role = role)
            val response = usersApi.updateUserRole(userId, request)

            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    Result.success(mapUserResponseToUser(userResponse))
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

    // ==================== Заявки на администратора ====================

    /**
     * Получить все заявки на администратора
     * GET /admin-requests
     */
    suspend fun getAllAdminRequests(): Result<List<AdminRequestDTO>> {
        return try {
            val response = adminRequestsApi.getAllAdminRequests()

            if (response.isSuccessful) {
                val requests = response.body() ?: emptyList()
                Result.success(requests)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    /**
     * Обновить статус заявки
     * PUT /admin-requests/{id}/status
     */
    suspend fun updateAdminRequestStatus(requestId: Int, status: String): Result<AdminRequestDTO> {
        return try {
            val request = AdminRequestStatusUpdateRequest(status = status)
            val response = adminRequestsApi.updateAdminRequestStatus(requestId, request)

            if (response.isSuccessful) {
                val updatedRequest = response.body()
                if (updatedRequest != null) {
                    Result.success(updatedRequest)
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
     * Создать заявку на администратора
     * POST /admin-requests
     */
    suspend fun createAdminRequest(userId: Int, text: String): Result<AdminRequestDTO> {
        return try {
            val request = AdminRequestCreateRequest(userId = userId, text = text)
            val response = adminRequestsApi.createAdminRequest(request)

            if (response.isSuccessful) {
                val createdRequest = response.body()
                if (createdRequest != null) {
                    Result.success(createdRequest)
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
     * Получить заявки пользователя
     * GET /admin-requests/user/{userId}
     */
    suspend fun getAdminRequestsByUserId(userId: Int): Result<List<AdminRequestDTO>> {
        return try {
            val response = adminRequestsApi.getAdminRequestsByUserId(userId)

            if (response.isSuccessful) {
                val requests = response.body() ?: emptyList()
                Result.success(requests)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        }
    }

    // ==================== Отели ====================

    /**
     * Получить все отели
     * Использует HotelsRepository
     */
    suspend fun getAllHotels(): Result<List<Hotel>> {
        return hotelsRepository.getAllHotels()
    }

    // ==================== Маппинг моделей ====================

    /**
     * Конвертация UserResponse (сервер) → User (приложение)
     */
    private fun mapUserResponseToUser(userResponse: UserResponse): User {
        return User(
            id = userResponse.id,
            name = userResponse.name,
            email = userResponse.email,
            password = "", // Пароль не возвращается сервером
            role = mapUserRole(userResponse.role),
            registrationDate = null
        )
    }

    /**
     * Конвертация строки роли с сервера в UserRole
     */
    private fun mapUserRole(roleString: String): UserRole {
        return when (roleString.lowercase()) {
            "user" -> UserRole.USER
            "hotel_admin", "hoteladmin" -> UserRole.HOTEL_ADMIN
            "system_admin", "systemadmin", "admin" -> UserRole.SYSTEM_ADMIN
            else -> UserRole.USER
        }
    }
}
