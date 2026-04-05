package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.testapp.data.model.Hotel
import com.example.testapp.data.model.HotelType
import com.example.testapp.data.model.SearchParams
import com.example.testapp.data.model.SortOption
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel для экрана поиска и результатов
 * Использует общий AppRepository через синглтон
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val cities = repository.getAllCities()
        val amenities = repository.getAllAmenities()
        val priceRange = repository.getPriceRange()

        _uiState.value = _uiState.value.copy(
            availableCities = cities,
            availableAmenities = amenities,
            minPrice = priceRange.start.toInt(),
            maxPrice = priceRange.endInclusive.toInt(),
            priceRangeStart = priceRange.start.toInt(),
            priceRangeEnd = priceRange.endInclusive.toInt()
        )

        performSearch()
    }

    fun selectCity(city: String?) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
    }

    fun setCheckInDate(date: String) {
        val state = _uiState.value
        // Проверка: дата заезда не может быть в прошлом
        val today = java.time.LocalDate.now().toString()
        if (date < today) {
            _uiState.value = state.copy(error = "Дата заезда не может быть в прошлом")
            return
        }

        // Если дата выезда раньше даты заезда, очищаем её
        val newCheckOutDate = if (state.checkOutDate != null && date >= state.checkOutDate) {
            null
        } else {
            state.checkOutDate
        }

        _uiState.value = state.copy(
            checkInDate = date,
            checkOutDate = newCheckOutDate,
            error = null
        )
    }

    fun setCheckOutDate(date: String) {
        val state = _uiState.value
        val checkInDate = state.checkInDate

        if (checkInDate != null && date <= checkInDate) {
            _uiState.value = state.copy(error = "Дата выезда должна быть позже даты заезда")
            return
        }

        _uiState.value = state.copy(checkOutDate = date, error = null)
    }

    fun setGuests(count: Int) {
        if (count < 1) {
            _uiState.value = _uiState.value.copy(error = "Количество гостей должно быть не менее 1")
            return
        }
        _uiState.value = _uiState.value.copy(guests = count, error = null)
    }

    fun setPriceRange(min: Int, max: Int) {
        _uiState.value = _uiState.value.copy(
            priceRangeStart = min,
            priceRangeEnd = max,
            error = null
        )
    }

    fun toggleHotelType(type: HotelType) {
        val state = _uiState.value
        val currentTypes = state.selectedHotelTypes
        val newTypes = if (type in currentTypes) {
            currentTypes - type
        } else {
            currentTypes + type
        }
        _uiState.value = state.copy(selectedHotelTypes = newTypes)
    }

    fun toggleAmenity(amenity: String) {
        val state = _uiState.value
        val currentAmenities = state.selectedAmenities
        val newAmenities = if (amenity in currentAmenities) {
            currentAmenities - amenity
        } else {
            currentAmenities + amenity
        }
        _uiState.value = state.copy(selectedAmenities = newAmenities)
    }

    fun setMinRating(rating: Float) {
        _uiState.value = _uiState.value.copy(minRating = rating)
    }

    fun setMinStars(stars: Int) {
        _uiState.value = _uiState.value.copy(minStars = stars)
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(currentSortOption = sortOption)
    }

    fun toggleSortMenu() {
        val state = _uiState.value
        _uiState.value = state.copy(showSortMenu = !state.showSortMenu)
    }

    fun toggleFilters() {
        val state = _uiState.value
        _uiState.value = state.copy(showFilters = !state.showFilters)
    }

    fun resetFilters() {
        val state = _uiState.value
        _uiState.value = state.copy(
            priceRangeStart = state.minPrice,
            priceRangeEnd = state.maxPrice,
            selectedHotelTypes = emptyList(),
            selectedAmenities = emptyList(),
            minRating = null,
            minStars = null
        )
    }

    fun performSearch() {
        val state = _uiState.value

        val params = SearchParams(
            city = state.selectedCity,
            checkInDate = state.checkInDate,
            checkOutDate = state.checkOutDate,
            guests = state.guests,
            priceMin = state.priceRangeStart.toDouble(),
            priceMax = state.priceRangeEnd.toDouble(),
            hotelTypes = state.selectedHotelTypes,
            minRating = state.minRating,
            minStars = state.minStars,
            amenities = state.selectedAmenities
        )

        _uiState.value = state.copy(isLoading = true)

        val results = repository.searchHotelsWithSort(params, state.currentSortOption)

        _uiState.value = state.copy(
            searchResults = results,
            isLoading = false,
            hasSearched = true,
            showFilters = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SearchUiState(
    val availableCities: List<String> = emptyList(),
    val availableAmenities: List<String> = emptyList(),
    val selectedCity: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val guests: Int = 2,
    val minPrice: Int = 0,
    val maxPrice: Int = 0,
    val priceRangeStart: Int = 0,
    val priceRangeEnd: Int = 0,
    val selectedHotelTypes: List<HotelType> = emptyList(),
    val selectedAmenities: List<String> = emptyList(),
    val minRating: Float? = null,
    val minStars: Int? = null,
    val currentSortOption: SortOption = SortOption.RATING_DESC,
    val showSortMenu: Boolean = false,
    val showFilters: Boolean = false,
    val searchResults: List<Hotel> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
