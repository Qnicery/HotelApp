package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.User
import com.example.testapp.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана профиля
 * 
 * Архитектура:
 * ProfileScreen → ProfileViewModel → ProfileRepository → API
 * 
 * При загрузке:
 * 1. GET /me — получение актуальных данных пользователя
 * 2. GET /bookings/user/{userId} — получение бронирований пользователя
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Загрузка профиля пользователя
     * 1. GET /me - получение актуальных данных пользователя
     * 2. GET /bookings/user/{userId} - получение бронирований пользователя
     */
    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Шаг 1: Запрашиваем актуальные данные пользователя с сервера
            repository.fetchCurrentUser()
                .onSuccess { user ->
                    // Шаг 2: Загружаем бронирования пользователя с сервера
                    loadBookings(user)
                }
                .onFailure { error ->
                    // Если ошибка с сервером, пробуем получить локально сохранённого
                    val localUser = repository.getCachedUser()
                    if (localUser != null) {
                        loadBookings(localUser, fallback = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Пользователь не авторизован"
                        )
                    }
                }
        }
    }

    /**
     * Загрузка бронирований пользователя через Repository
     */
    private suspend fun loadBookings(user: User, fallback: Boolean = false) {
        repository.getBookingsByUserId(user.id)
            .onSuccess { bookings ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    bookings = bookings,
                    isLoading = false,
                    error = null
                )
            }
            .onFailure { error ->
                if (fallback) {
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        bookings = emptyList(),
                        isLoading = false,
                        error = "Не удалось загрузить бронирования"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        bookings = emptyList(),
                        isLoading = false,
                        error = "Ошибка загрузки бронирований: ${error.message}"
                    )
                }
            }
    }

    /**
     * Обновить профиль (перезагрузить с сервера)
     */
    fun refreshProfile() {
        loadProfile()
    }

    /**
     * Отменить бронирование через Repository
     * PUT /bookings/{id}/status { "status": "Canceled" }
     */
    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch

            repository.cancelBooking(bookingId)
                .onSuccess {
                    // Перезагружаем бронирования после отмены
                    loadBookings(currentUser)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка при отмене бронирования: ${error.message}"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = ProfileUiState()
        }
    }

    fun getUser(): User? = repository.getCachedUser()
}

data class ProfileUiState(
    val user: User? = null,
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
