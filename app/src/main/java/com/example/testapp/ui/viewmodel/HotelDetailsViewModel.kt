package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.api.ServerConfig
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.Review
import com.example.testapp.data.model.Room
import com.example.testapp.data.repository.HotelsRepository
import com.example.testapp.data.repository.ReviewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана деталей отеля
 * 
 * Архитектура:
 * HotelDetailsScreen → HotelDetailsViewModel → HotelsRepository → API
 * 
 * Загружает:
 * - GET /hotels/{id} — информация об отеле
 * - GET /rooms/hotel/{hotelId} — комнаты отеля
 */
class HotelDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val hotelsRepository = HotelsRepository.getInstance(application)
    private val reviewsRepository = ReviewsRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HotelDetailsUiState())
    val uiState: StateFlow<HotelDetailsUiState> = _uiState.asStateFlow()

    /**
     * Загрузить информацию об отеле
     * GET /hotels/{id}
     */
    fun loadHotelDetails(hotelId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            hotelsRepository.getHotelById(hotelId)
                .onSuccess { hotel ->
                    // Загружаем комнаты и отзывы
                    loadRoomsAndReviews(hotelId)
                    
                    _uiState.value = _uiState.value.copy(
                        hotel = hotel,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки отеля: ${error.message}"
                    )
                }
        }
    }

    /**
     * Загрузить комнаты и отзывы
     */
    private fun loadRoomsAndReviews(hotelId: Int) {
        viewModelScope.launch {
            // Загружаем комнаты с сервера
            hotelsRepository.getRoomsByHotelId(hotelId)
                .onSuccess { roomDTOs ->
                    val rooms = roomDTOs.map { dto ->
                        Room(
                            id = dto.id,
                            hotelId = dto.hotelId,
                            name = dto.roomName,
                            description = dto.description ?: "",
                            imageUrl = ServerConfig.getImageUrl(dto.photoUrls?.firstOrNull()),
                            pricePerNight = dto.price,
                            maxGuests = dto.maxGuests,
                            amenities = emptyList(), // TODO: Загружать через GET /room-amenities/room/{id} — см. TODO.md
                            isAvailable = dto.status.equals("available", ignoreCase = true)
                        )
                    }
                    _uiState.value = _uiState.value.copy(rooms = rooms)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Ошибка загрузки комнат: ${error.message}"
                    )
                }

            // Загружаем отзывы с сервера
            reviewsRepository.getReviewsByHotelId(hotelId)
                .onSuccess { stats ->
                    val reviews = reviewsRepository.mapReviewDTOsToReviews(stats.reviews)
                    _uiState.value = _uiState.value.copy(reviews = reviews)
                }
                .onFailure { error ->
                    // Не блокируем UI из-за ошибки отзывов
                    _uiState.value = _uiState.value.copy(reviews = emptyList())
                }
        }
    }

    fun toggleAmenitiesExpanded() {
        _uiState.value = _uiState.value.copy(areAmenitiesExpanded = !_uiState.value.areAmenitiesExpanded)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HotelDetailsUiState(
    val hotel: Hotel? = null,
    val rooms: List<Room> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = true,
    val areAmenitiesExpanded: Boolean = false,
    val error: String? = null
)
