package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.model.City
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.repository.HotelsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана со списком отелей
 * 
 * Архитектура:
 * HomeScreen → HotelsViewModel → HotelsRepository → API
 * 
 * Загружает отели и города с сервера:
 * - GET /hotels — все отели
 * - GET /hotels/city/{city} — отели по городу
 */
class HotelsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HotelsRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HotelsUiState())
    val uiState: StateFlow<HotelsUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
        loadCities()
    }

    /**
     * Загрузить все отели
     */
    private fun loadHotels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getAllHotels()
                .onSuccess { hotels ->
                    _uiState.value = _uiState.value.copy(
                        hotels = hotels,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        hotels = emptyList(),
                        isLoading = false,
                        error = "Ошибка загрузки отелей: ${error.message}"
                    )
                }
        }
    }

    /**
     * Загрузить список городов
     */
    private fun loadCities() {
        viewModelScope.launch {
            repository.getCities()
                .onSuccess { cities ->
                    _uiState.value = _uiState.value.copy(cities = cities)
                }
                .onFailure { error ->
                    // Не блокируем UI из-за ошибки городов
                    _uiState.value = _uiState.value.copy(cities = emptyList())
                }
        }
    }

    /**
     * Выбрать город для фильтрации
     */
    fun selectCity(cityName: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCity = cityName,
            isLoading = true
        )

        viewModelScope.launch {
            if (cityName == null) {
                // Загрузить все отели
                loadHotels()
            } else {
                // Загрузить отели по городу
                repository.getHotelsByCity(cityName)
                    .onSuccess { hotels ->
                        _uiState.value = _uiState.value.copy(
                            hotels = hotels,
                            isLoading = false,
                            error = null
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            hotels = emptyList(),
                            isLoading = false,
                            error = "Ошибка загрузки отелей: ${error.message}"
                        )
                    }
            }
        }
    }

    /**
     * Обновить список отелей (перезагрузить с сервера)
     */
    fun refreshHotels() {
        loadHotels()
        loadCities()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HotelsUiState(
    val hotels: List<Hotel> = emptyList(),
    val cities: List<City> = emptyList(),
    val selectedCity: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
