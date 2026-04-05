package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.model.Review
import com.example.testapp.data.repository.AuthApiRepository
import com.example.testapp.data.repository.ReviewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана отзывов
 * 
 * Архитектура:
 * ReviewsScreen → ReviewsViewModel → ReviewsRepository → API
 * 
 * Загружает:
 * - GET /reviews/hotel/{hotelId} — отзывы отеля со статистикой
 * - POST /reviews — создать отзыв
 */
class ReviewsViewModel(application: Application) : AndroidViewModel(application) {

    private val reviewsRepository = ReviewsRepository.getInstance(application)
    private val authRepository = AuthApiRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    /**
     * Загрузить отзывы отеля
     * GET /reviews/hotel/{hotelId}
     */
    fun loadReviews(hotelId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            reviewsRepository.getReviewsByHotelId(hotelId)
                .onSuccess { stats ->
                    val reviews = reviewsRepository.mapReviewDTOsToReviews(stats.reviews)
                    _uiState.value = _uiState.value.copy(
                        reviews = reviews,
                        averageRating = stats.averageRating.toFloat(),
                        reviewCount = stats.reviewCount,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        reviews = emptyList(),
                        isLoading = false,
                        error = "Ошибка загрузки отзывов: ${error.message}"
                    )
                }
        }
    }

    /**
     * Добавить отзыв
     * POST /reviews
     * 
     * TODO: Когда сервер добавит получение bookingId для отеля — реализовать создание отзыва
     * Сейчас для создания отзыва нужен bookingId, который мы пока не можем получить
     */
    fun addReview(hotelId: Int, rating: Float, text: String) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
                ?: run {
                    _uiState.value = _uiState.value.copy(error = "Необходимо авторизоваться")
                    return@launch
                }

            if (text.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Введите текст отзыва")
                return@launch
            }

            if (rating < 1f || rating > 5f) {
                _uiState.value = _uiState.value.copy(error = "Рейтинг должен быть от 1 до 5")
                return@launch
            }

            // TODO: Нужен bookingId для создания отзыва
            // Пока показываем ошибку что bookingId недоступен
            _uiState.value = _uiState.value.copy(
                error = "Создание отзыва временно недоступно (требуется bookingId)"
            )
            
            /* Когда сервер добавит endpoint для получения bookingId:
            val bookingId = getBookingIdForHotel(hotelId, currentUser.id)
            
            reviewsRepository.createReview(
                bookingId = bookingId,
                hotelId = hotelId,
                rating = rating.toInt(),
                text = text
            )
                .onSuccess { review ->
                    val currentState = _uiState.value
                    _uiState.value = currentState.copy(
                        reviews = currentState.reviews + review,
                        reviewSuccess = true,
                        error = null,
                        newRating = 0f,
                        newReviewText = ""
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            */
        }
    }

    fun updateRating(rating: Float) {
        _uiState.value = _uiState.value.copy(newRating = rating)
    }

    fun updateReviewText(text: String) {
        _uiState.value = _uiState.value.copy(newReviewText = text)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetReviewForm() {
        val state = _uiState.value
        _uiState.value = state.copy(
            newRating = 0f,
            newReviewText = "",
            reviewSuccess = false
        )
    }
}

data class ReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val averageRating: Float = 0f,
    val reviewCount: Int = 0,
    val newRating: Float = 0f,
    val newReviewText: String = "",
    val isLoading: Boolean = true,
    val reviewSuccess: Boolean = false,
    val error: String? = null
)
