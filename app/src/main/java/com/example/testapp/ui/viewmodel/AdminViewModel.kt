package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.api.model.AdminRequestDTO
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.User
import com.example.testapp.data.model.UserRole
import com.example.testapp.data.repository.AdminSystemRepository
import com.example.testapp.data.repository.HotelsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для админ-панели отеля
 * Использует HotelsRepository для работы с данными
 */
class AdminHotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HotelsRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AdminHotelUiState())
    val uiState: StateFlow<AdminHotelUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
    }

    private fun loadHotels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getAllHotels()
            _uiState.value = _uiState.value.copy(
                hotels = if (result.isSuccess) result.getOrNull() ?: emptyList() else emptyList(),
                isLoading = false,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AdminHotelUiState(
    val hotels: List<Hotel> = emptyList(),
    val rooms: List<com.example.testapp.data.api.model.RoomDTO> = emptyList(),
    val bookings: List<com.example.testapp.data.model.Booking> = emptyList(),
    val reviews: List<com.example.testapp.data.model.Review> = emptyList(),
    val selectedHotelId: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel для системного администратора
 * Использует AdminSystemRepository для работы с реальными данными
 */
class AdminSystemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdminSystemRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AdminSystemUiState())
    val uiState: StateFlow<AdminSystemUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Загрузить все данные (пользователи, заявки, отели)
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Загружаем данные параллельно
                val usersResult = repository.getAllUsers()
                val adminRequestsResult = repository.getAllAdminRequests()
                val hotelsResult = repository.getAllHotels()

                _uiState.value = _uiState.value.copy(
                    users = if (usersResult.isSuccess) usersResult.getOrNull() ?: emptyList() else emptyList(),
                    adminRequests = if (adminRequestsResult.isSuccess) adminRequestsResult.getOrNull() ?: emptyList() else emptyList(),
                    hotels = if (hotelsResult.isSuccess) hotelsResult.getOrNull() ?: emptyList() else emptyList(),
                    isLoading = false,
                    error = when {
                        usersResult.isFailure -> usersResult.exceptionOrNull()?.message
                        adminRequestsResult.isFailure -> adminRequestsResult.exceptionOrNull()?.message
                        hotelsResult.isFailure -> hotelsResult.exceptionOrNull()?.message
                        else -> null
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Обновить статус заявки на администратора
     */
    fun updateAdminRequestStatus(requestId: Int, status: String) {
        viewModelScope.launch {
            val result = repository.updateAdminRequestStatus(requestId, status)
            if (result.isSuccess) {
                // Перезагружаем заявки
                val adminRequestsResult = repository.getAllAdminRequests()
                if (adminRequestsResult.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        adminRequests = adminRequestsResult.getOrNull() ?: emptyList()
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * Принять заявку - обновить статус И изменить роль пользователя
     */
    fun approveAdminRequest(requestId: Int, userId: Int) {
        viewModelScope.launch {
            // Сначала обновляем роль пользователя на HOTEL_ADMIN
            val roleResult = repository.updateUserRole(userId, "Hotel_Admin")
            
            if (roleResult.isSuccess) {
                // Затем обновляем статус заявки
                val requestResult = repository.updateAdminRequestStatus(requestId, "Approved")
                
                if (requestResult.isSuccess) {
                    // Перезагружаем данные
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = requestResult.exceptionOrNull()?.message
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    error = roleResult.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * Отклонить заявку
     */
    fun rejectAdminRequest(requestId: Int) {
        updateAdminRequestStatus(requestId, "Rejected")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AdminSystemUiState(
    val users: List<User> = emptyList(),
    val adminRequests: List<AdminRequestDTO> = emptyList(),
    val hotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
