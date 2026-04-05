package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.testapp.data.model.City
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel для главного экрана со списком отелей
 * Использует общий AppRepository через синглтон
 */
class HotelsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HotelsUiState())
    val uiState: StateFlow<HotelsUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
        loadCities()
    }

    private fun loadHotels() {
        val hotels = repository.getHotels()
        _uiState.value = _uiState.value.copy(
            hotels = hotels,
            isLoading = false
        )
    }

    private fun loadCities() {
        val cities = repository.getCities()
        _uiState.value = _uiState.value.copy(cities = cities)
    }

    fun selectCity(cityName: String?) {
        _uiState.value = _uiState.value.copy(selectedCity = cityName)
        if (cityName == null) {
            loadHotels()
        } else {
            val filtered = repository.getHotels().filter { it.city == cityName }
            _uiState.value = _uiState.value.copy(hotels = filtered)
        }
    }
}

data class HotelsUiState(
    val hotels: List<Hotel> = emptyList(),
    val cities: List<City> = emptyList(),
    val selectedCity: String? = null,
    val isLoading: Boolean = true
)
