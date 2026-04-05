package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.Notification
import com.example.testapp.data.model.User
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана профиля
 * Использует общий AppRepository через синглтон
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                if (user != null) {
                    val bookings = repository.getBookingsByUser(user.id)
                    val notifications = repository.getNotificationsByUser(user.id)
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        bookings = bookings,
                        notifications = notifications,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Пользователь не авторизован"
                    )
                }
            }
        }
    }

    fun cancelBooking(bookingId: Int): Result<Unit> {
        return repository.cancelBooking(bookingId)
            .onSuccess {
                loadProfile()
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
    }

    fun markNotificationAsRead(notificationId: Int) {
        repository.markNotificationAsRead(notificationId)
        loadProfile()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getUser(): User? = repository.getCurrentUser()
}

data class ProfileUiState(
    val user: User? = null,
    val bookings: List<Booking> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
