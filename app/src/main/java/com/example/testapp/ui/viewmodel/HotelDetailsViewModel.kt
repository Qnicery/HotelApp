package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.Review
import com.example.testapp.data.model.Room
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel для экрана деталей отеля
 * Использует общий AppRepository через синглтон
 */
class HotelDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HotelDetailsUiState())
    val uiState: StateFlow<HotelDetailsUiState> = _uiState.asStateFlow()

    fun loadHotelDetails(hotelId: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        val hotel = repository.getHotelById(hotelId)
        if (hotel != null) {
            val rooms = repository.getRoomsByHotel(hotelId)
            val reviews = repository.getReviewsByHotel(hotelId)
            _uiState.value = _uiState.value.copy(
                hotel = hotel,
                rooms = rooms,
                reviews = reviews,
                isLoading = false,
                error = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Отель не найден"
            )
        }
    }

    fun toggleAmenitiesExpanded() {
        _uiState.value = _uiState.value.copy(areAmenitiesExpanded = !_uiState.value.areAmenitiesExpanded)
    }

    fun loadReviews(hotelId: Int) {
        val reviews = repository.getReviewsByHotel(hotelId)
        _uiState.value = _uiState.value.copy(reviews = reviews)
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
