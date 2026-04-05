package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.Review
import com.example.testapp.data.model.Room
import com.example.testapp.data.model.User
import com.example.testapp.data.model.UserRole
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel для админ-панели отеля
 * Использует общий AppRepository через синглтон
 */
class AdminHotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AdminHotelUiState())
    val uiState: StateFlow<AdminHotelUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
    }

    fun loadHotels() {
        val hotels = repository.getHotelsForAdmin()
        _uiState.value = _uiState.value.copy(hotels = hotels, isLoading = false)
    }

    fun loadRooms(hotelId: Int) {
        val rooms = repository.getRoomsForAdmin(hotelId)
        _uiState.value = _uiState.value.copy(
            rooms = rooms,
            selectedHotelId = hotelId,
            isLoading = false
        )
    }

    fun updateRoomAvailability(roomId: Int, isAvailable: Boolean): Result<Unit> {
        return repository.updateRoomAvailability(roomId, isAvailable)
            .onSuccess {
                val state = _uiState.value
                _uiState.value = state.copy(
                    rooms = state.rooms.map { room ->
                        if (room.id == roomId) room.copy(isAvailable = isAvailable) else room
                    }
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AdminHotelUiState(
    val hotels: List<Hotel> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val selectedHotelId: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel для системного администратора
 * Использует общий AppRepository через синглтон
 */
class AdminSystemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AdminSystemUiState())
    val uiState: StateFlow<AdminSystemUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        val users = repository.getAllUsers()
        _uiState.value = _uiState.value.copy(
            users = users,
            isLoading = false
        )
    }

    fun updateUserRole(userId: Int, newRole: UserRole): Result<Unit> {
        return repository.updateUserRole(userId, newRole)
            .onSuccess {
                loadUsers()
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AdminSystemUiState(
    val users: List<User> = emptyList(),
    val pendingRequests: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
